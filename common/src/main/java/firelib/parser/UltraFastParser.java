package firelib.parser;

import firelib.common.reader.MarketDataReader;
import firelib.domain.Timed;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * FIXME not completed parser to handle multiple files via prev/nextfile in common.ini
 * @param <T>
 */

public class UltraFastParser<T extends Timed> implements MarketDataReader<T> {
    private static Instant maxDateTime = Instant.MAX;
    private static Instant minDateTime = Instant.MIN;
    private MarketDataReader<T> csvParser = null;
    private SymbolCsvFileInfo[] symbolCsvFileInfo = null;
    private int fileIdx = -1;

    public UltraFastParser(String csvFileName) throws Exception {

        if (!Files.exists(Paths.get(csvFileName))) {
            throw new RuntimeException("Error: input file '" + csvFileName + "' does not exist.");
        }

        FileInfo fileInfo = ParseSymbolFileName(csvFileName);
        symbolCsvFileInfo = EnumerateTickerFiles(fileInfo.strPath, fileInfo.strSymbol, fileInfo.strExt);
    }


    private void OpenCurrentCsvFile() {
        csvParser = create(symbolCsvFileInfo[fileIdx].fullFileName, symbolCsvFileInfo[fileIdx].legacyMarketDataFormat);
        csvParser.seek(csvParser.startTime());
        CurrentTz = symbolCsvFileInfo[fileIdx].legacyMarketDataFormat.TIMEZONE;
    }

    MarketDataReader<T> create(String fileName, LegacyMarketDataFormat settings) {
        return null;
    }

    public String CurrentTz;

    @Override
    public T current() {
        return csvParser.current();
    }

    public boolean seek(Instant utcDT) {
        fileIdx = -1;
        csvParser = null;

        if (symbolCsvFileInfo.length == 0) {
            return true;
        }

        for (int i = 0; i < symbolCsvFileInfo.length; ++i) {
            if (utcDT.isAfter(symbolCsvFileInfo[i].utcStartDT)) {
                fileIdx = i;
                break;
            }
        }

        if (fileIdx == -1) {
            //
            // dtloc is very very old date. Set index to point to oldest .csv file.
            //
            fileIdx = symbolCsvFileInfo.length - 1;
        }

        OpenCurrentCsvFile();

        if (utcDT.isBefore(symbolCsvFileInfo[fileIdx].utcStartDT)) {
            utcDT = symbolCsvFileInfo[fileIdx].utcStartDT;
        }

        if (utcDT.isAfter(symbolCsvFileInfo[fileIdx].utcEndDT)) {
            utcDT = symbolCsvFileInfo[fileIdx].utcEndDT;
        }

        boolean seekLocal = csvParser.seek(utcDT);
        return seekLocal;
    }


    public boolean read() {
        if (csvParser == null)
            return false;


        if (csvParser.read() && (csvParser.current().time().isBefore(symbolCsvFileInfo[fileIdx].utcEndDT.plusMillis(1)))) {
            return true;
        }

        //
        // Switch to next .csv file.
        //
        if (fileIdx == 0) {
            return false;
        }

        fileIdx--;
        OpenCurrentCsvFile();
        return csvParser.read();
    }

    @Override
    public Instant startTime() {
        return csvParser.startTime();
    }

    @Override
    public Instant endTime() {
        return csvParser.endTime();
    }


    public void Dispose() {
        try {
            csvParser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        csvParser = null;
        symbolCsvFileInfo = null;
        fileIdx = -1;
    }

    public static String ExpandRelativeChainPathToAbsolute(String strCommonIniPath, String strChainPath) {
        String strBasePath = Paths.get(strCommonIniPath).getParent().toString();

        if (!new File(strChainPath).isAbsolute()) {
            strChainPath = Paths.get(strBasePath, strChainPath).toAbsolutePath().toString();
        }

        return strChainPath;
    }


    //
    // ??????? ??????? common.ini ????, ? ??????? ????????? ? ????, ??? ??? ?????????? ? commonIniSettings.
    //
    public static void ParseAndMergeCommonIni(String strFileName, LegacyMarketDataFormat legacyMarketDataFormat) throws Exception {
        Properties settings = new Properties();
        settings.load(new FileInputStream(strFileName));

        if (settings.containsKey("NEXTFILE")) {
            legacyMarketDataFormat.NEXTFILE = ExpandRelativeChainPathToAbsolute(strFileName,
                    settings.getProperty("NEXTFILE"));
        }

        if (settings.containsKey("PREVFILE")) {
            legacyMarketDataFormat.PREVFILE = ExpandRelativeChainPathToAbsolute(strFileName,
                    settings.getProperty("PREVFILE"));
        }


        if (settings.containsKey("DATEFORMAT")) {
            legacyMarketDataFormat.DATEFORMAT = settings.getProperty("DATEFORMAT");
        }

        if (settings.containsKey("TIMEFORMAT")) {
            legacyMarketDataFormat.TIMEFORMAT = settings.getProperty("TIMEFORMAT");
        }

        if (settings.containsKey("COLUMNFORMAT")) {
            legacyMarketDataFormat.COLUMNFORMAT = settings.getProperty("COLUMNFORMAT").split("_");
        }

        if (settings.containsKey("TIMEZONE")) {
            legacyMarketDataFormat.TIMEZONE = settings.getProperty("TIMEZONE");
        }
    }

    @Override
    public void close() throws Exception {

    }


    //
    // Parses given symbol file name and returns <path>, <symbol> and <ext> components.
    //
    // NOTE: Path is always returned in absolute form.
    //
    // For example, for input String:
    //
    //    .\Tests\TickersSorting\RI#_0.csv
    //
    // Returns:
    //     strPath   = @"C:\GLOBALDATABASE\Tests\TickersSorting"
    //     strSymbol = @"RI#"
    //     strExt    = @".csv"
    //


    public static class FileInfo {
        public String strPath;
        public String strSymbol;
        public String strExt;

        FileInfo(String strPath, String strSymbol, String strExt) {
            this.strPath = strPath;
            this.strSymbol = strSymbol;
            this.strExt = strExt;
        }
    }


    public static FileInfo ParseSymbolFileName(String fullPathName) {
        return new FileInfo(Paths.get(fullPathName).getParent().toString(), Paths.get(fullPathName).getFileName().toString(), null);
    }

    //
    // This routine is initially called by EnumerateTickerFiles() with level == 0 and empty visitedPathList.
    // Then EnumerateTickerFilesHelper() recursively calls itself to walk through all PREVFILE references.
    //
    public SymbolCsvFileInfo[] EnumerateTickerFilesHelper(String strPath, String strSymbol, String strExt,
                                                          MutableObject<Instant> cutDatesHigherThan, MutableInt level,
                                                          List<String> visitedPathList) throws Exception {
        //
        // Normalize strPath. And check where we already visited it.
        //
        //strPath = strPath.TrimEnd('\\');
        //strPath = strPath.TrimEnd('/');


        if (visitedPathList.contains(strPath)) {
            throw new RuntimeException("EnumerateTickerFilesHelper() - found a loop!");
        }

        if (level.intValue() > 1000) {
            throw new RuntimeException("EnumerateTickerFilesHelper() - found a loop (level > 1000)!");
        }

        level.increment();
        visitedPathList.add(strPath);

        //
        // Ok, enumerate ticker files and read common.ini PREVFILE entries.
        //


        //Files.list(Paths.get()).filter(s -> s.))

        File dir = new File(strPath);
        final String finalStrExt = strExt;
        final String finalStrSymbol = strSymbol;
        File[] filesList = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(finalStrSymbol + "_") && name.endsWith(finalStrExt);
            }
        });


        LegacyMarketDataFormat legacyMarketDataFormat = new LegacyMarketDataFormat();
        ParseAndMergeCommonIni(Paths.get(strPath, "common.ini").toString(), legacyMarketDataFormat);

        //
        // Walk though files list in current folder and check for PREVFILE in individual .ini files,
        // fill in SymbolCsvFileInfo structures, and generate a sorted list.
        //
        List<SymbolCsvFileInfo> tmpList = new ArrayList<SymbolCsvFileInfo>();

        for (File fn : filesList) {
            String filename = fn.getAbsolutePath();

            SymbolCsvFileInfo entry = new SymbolCsvFileInfo();
            entry.fullFileName = filename;

            //
            // Read and merge common.ini settings with individual .ini file settings.
            //
            String strCommonIniFile = Paths.get(filename).getParent().resolve("common.ini").toString();
            String strIndividualIniFile = Paths.get(filename).getParent().resolve(filename).toString(); //FIXME replace last with ini


            ParseAndMergeCommonIni(strCommonIniFile, entry.legacyMarketDataFormat);

            if (Files.exists(Paths.get(strIndividualIniFile))) {
                ParseAndMergeCommonIni(strIndividualIniFile, entry.legacyMarketDataFormat);
            }

            MarketDataReader<T> reader = create(filename, entry.legacyMarketDataFormat);

            entry.utcStartDT = reader.startTime();
            entry.utcEndDT = reader.endTime();

            reader.close();


        }

        Collections.sort(tmpList);


        //
        // Remove all entries that
        //
        List<SymbolCsvFileInfo> res = new ArrayList<>();
        String prevFileReference = legacyMarketDataFormat.PREVFILE;

        for (SymbolCsvFileInfo entry : tmpList) {
            if (entry.utcStartDT.isAfter(cutDatesHigherThan.getValue())) {
                continue;
            }

            //
            // Fix endDate for this .csv file entry if last trade date in .csv file if higher than cutDatesHigherThan.
            // (i.e. .csv file partially overlaps with oldest file data in another folder.
            //
            if (!entry.utcEndDT.equals(maxDateTime) && entry.utcEndDT.isAfter(cutDatesHigherThan.getValue())) {
                entry.utcEndDT = cutDatesHigherThan.getValue();
            }

            if (entry.utcStartDT.toEpochMilli() != 0) {
                cutDatesHigherThan.setValue(entry.utcStartDT);
            }

            //
            // Sanity check.
            //

            if (entry.utcEndDT.isBefore(entry.utcStartDT)) {
                throw new RuntimeException("Error: utcEndDT < utcStartDT for file: " + entry.fullFileName);
            }

            //
            // Only add non-zero length .csv files to results array (since we can not determine start/end date
            // for files that do not contain at least one line).
            //
            if (entry.utcStartDT.toEpochMilli() != 0 || entry.utcEndDT.toEpochMilli() != 0) {
                res.add(entry);
            }

            //
            // Handle PREVFILE reference from individual .ini file.
            //


            String strIndividualIniFile = Paths.get(removeExtension(entry.fullFileName) + ".ini").toString();

            if (Files.exists(Paths.get(strIndividualIniFile))) {
                LegacyMarketDataFormat individualIniSettings = new LegacyMarketDataFormat();
                ParseAndMergeCommonIni(strIndividualIniFile, individualIniSettings);

                if (!(individualIniSettings.PREVFILE == null || individualIniSettings.PREVFILE.trim().length() == 0)) {
                    //
                    // Save found PREVFILE reference and stop processing here, since all files older than
                    // cutDatesHigherThan should come from another directory.
                    //
                    prevFileReference = individualIniSettings.PREVFILE;
                    break;
                }
            }
        }

        //
        // Process PREVFILE references recursively.
        //
        if (prevFileReference != null && prevFileReference.trim().length() > 0) {
            if (Files.exists(Paths.get(prevFileReference))) {
                //
                // For PREVFILE reference on individual files symbol name and extension can differ.
                // So extract them here from symbol file name.
                //

                FileInfo fileInfo = ParseSymbolFileName(prevFileReference);

                strSymbol = fileInfo.strSymbol;
                strExt = fileInfo.strExt;
                //
                // If we have a reference to file, remove filename and leave only directory part.
                //
                prevFileReference = Paths.get(prevFileReference).getParent().toString();
            }

            if (Files.exists(Paths.get(prevFileReference))) {
                res.addAll(Arrays.asList(EnumerateTickerFilesHelper(prevFileReference, strSymbol, strExt,
                        cutDatesHigherThan, level, visitedPathList)));
            } else {
                throw new RuntimeException("Error: invalid PREVFILE reference, dir or file does not exist: " + prevFileReference);
            }
        }

        return res.toArray(new SymbolCsvFileInfo[0]);
    }

    String removeExtension(String str) {
        return str.substring(0, str.lastIndexOf('.'));
    }

    //
    // Enumerates all ticker files for given strPath, strSymbol, strExt.
    //
    // Sorts ticker files names in historical order.
    // Returns tickerFileNames array. tickerFileNames[0] contains the newest file.
    //
    public SymbolCsvFileInfo[] EnumerateTickerFiles(String strPath, String strSymbol, String strExt) throws Exception {
        MutableInt level = new MutableInt(0);
        ArrayList visitedPathList = new ArrayList<String>();
        Instant cutDatesHigherThan = Instant.MAX;
        strPath = Paths.get(strPath).toString();
        SymbolCsvFileInfo[] filesList = EnumerateTickerFilesHelper(strPath, strSymbol, strExt,
                new MutableObject<>(cutDatesHigherThan), level,
                visitedPathList);


        Arrays.sort(filesList);
        return filesList;
    }

    //
    // ?????????, ??????? ????????? .csv ???? ? ???????. ??????????????, ????????? ? ???????? ???????? ???,
    // ??????? ??????? ?? ????? ?????, ??????????? ????????? ?? common.ini ? ??????????????? .ini ?????.
    //
    public static class SymbolCsvFileInfo implements Comparable<SymbolCsvFileInfo> {
        public LegacyMarketDataFormat legacyMarketDataFormat;
        // ????????? ?? common.ini ???????????? ? ?????????????? .ini ??????.
        public String fullFileName; // ?????????? ???? ? ??? .csv ?????.
        public Instant utcStartDT;
        public Instant utcEndDT;

        @Override
        public int compareTo(SymbolCsvFileInfo o) {
            return utcStartDT.compareTo(o.utcStartDT);
        }
    }

}
