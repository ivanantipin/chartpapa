package at.atserverapiexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import at.feedapi.ActiveTickServerAPI;
import at.feedapi.Helpers;
import at.shared.ATServerAPIDefines;
import at.shared.ATServerAPIDefines.ATBarHistoryType;
import at.shared.ATServerAPIDefines.ATConstituentListType;
import at.shared.ATServerAPIDefines.ATGUID;
import at.shared.ATServerAPIDefines.ATQuoteFieldType;
import at.shared.ATServerAPIDefines.ATSYMBOL;
import at.shared.ATServerAPIDefines.ATStreamRequestType;
import at.shared.ATServerAPIDefines.ATSymbolType;
import at.shared.ATServerAPIDefines.SYSTEMTIME;
import at.utils.jlib.Errors;

public class ATServerAPIExample extends Thread
{
	public static ActiveTickServerAPI serverapi;
	public static APISession apiSession;
	
	public void PrintUsage()
	{
		System.out.println("ActiveTick Feed Java API");
		System.out.println("Available commands:");
		System.out.println("-------------------------------------------------------------");
		System.out.println("?");
		System.out.println("quit");

		System.out.println("init [serverHostname] [serverPort] [apiKey] [userid] [password]");
		System.out.println("\tserverHostname: activetick1.activetick.com serverPort: 443");
		System.out.println("\tapiUserId: valid alphanumeric apiKey, for example EF1C0A768BBB11DFBCB3F923E0D72085");
		System.out.println("\tuserid and password: valid account login credentials");
		System.out.println("\texample: init activetick1.activetick.com 443 EF1C0A768BBB11DFBCB3F923E0D72085 myuser mypass");
		System.out.println("");
		
		System.out.println("getIntradayHistoryBars [symbol] [minutes] [beginTime] [endTime]");
		System.out.println("\tminutes: intraday bar minute interval, for example 1-minute, 5-minute bars");
		System.out.println("");
		
		System.out.println("getDailyHistoryBars [symbol] [beginTime] [endTime]");
		System.out.println("getWeeklyHistoryBars [symbol] [beginTime] [endTime]");
		System.out.println("");
		
		System.out.println("getTicks [symbol] [beginTime] [endTime]");
		System.out.println("getTicks [symbol] [number of records]");
		System.out.println("");
		
		System.out.println("getMarketMovers [symbol] [exchange]");
		System.out.println("\tsymbol: \"VL\"=volume, \"NG\"/\"NL\"=net gain/loser, \"PG\"/\"PL\"=percent gain/loser, ");
		System.out.println("\texchange: A=Amex, N=NYSE, Q=NASDAQ, U=OTCBB");
		System.out.println("\texample: getMarketMovers VL Q");
		System.out.println("");
		
		System.out.println("getQuoteDb [symbol]");
		System.out.println("getOptionChain [symbol]");
		System.out.println("");
		
		System.out.println("subscribeMarketMovers [symbol] [exchange]");
		System.out.println("unsubscribeMarketMovers [symbol] [exchange]");
		
		System.out.println("subscribeQuoteStream [symbol]");
		System.out.println("unsubscribeQuoteStream [symbol]");

		System.out.println("unsubscribeQuotesOnlyQuoteStream [symbol]");
		System.out.println("unsubscribeQuotesOnlyQuoteStream [symbol]");

		System.out.println("subscribeTradesOnlyQuoteStream [symbol]");
		System.out.println("unsubscribeTradesOnlyQuoteStream [symbol]");
		System.out.println("");
		
		System.out.println("-------------------------------------------------------------");
		System.out.println("Date/time format: YYYYMMDDHHMMSS");		
		System.out.println("Symbol format: stocks=GOOG, indeces=$DJI, currencies=#EUR/USD, options=.AAPL--131004C00380000");
		System.out.println("-------------------------------------------------------------");
	}
	
	public void InvalidGuidMessage()
	{
		System.out.println("Warning! \n\tApiUserIdGuid should be 32 characters long and alphanumeric only.");
	}
	
	public ATServerAPIExample()
	{
		PrintUsage();			
		start();	//get into the run method		
	}
	
	/**********************************************************************
	 * //processInput
	 * Notes:
	 * -Process command line input
	 **********************************************************************/

	public void processInput(String userInput)
	{
		StringTokenizer st = new StringTokenizer(userInput);
		List ls = new ArrayList<String>();
		while(st.hasMoreTokens())
			ls.add(st.nextToken());
		int count = ls.size();
		
		if(count > 0 && ((String)ls.get(0)).equalsIgnoreCase("?"))
		{
			PrintUsage();
		}

		//init
		else if(count >= 5 && ((String)ls.get(0)).equalsIgnoreCase("init"))
		{
			String serverHostname = ls.get(1).toString();
			int serverPort = new Integer(ls.get(2).toString());
			String apiKey = ls.get(3).toString();
			String userId = ls.get(4).toString();
			String password = ls.get(5).toString();
			
			if(apiKey.length() != 32)
			{
				InvalidGuidMessage();
				return;
			}
			
			ATGUID atguid = (new ATServerAPIDefines()).new ATGUID();
			atguid.SetGuid(apiKey);
			
			boolean rc = apiSession.Init(atguid, serverHostname, serverPort, userId, password);
			System.out.println("\ninit status: " + (rc == true ? "ok" : "failed"));
		}

		/**********************************************************************
		 * //getQuoteDb
		 * Examples:
		 * 		getQuoteDb AAPL
		 * 		getQuoteDb AAPL,AMZN
		 * 		getQuoteDb AAPL,ADBE,AMZN,ACLS,AKAM,ASTM,AAWW,ABMD,ACAT
		 **********************************************************************/
		else if(count >= 2 && ((String)ls.get(0)).equalsIgnoreCase("getQuoteDb"))
		{
			String strSymbols = ls.get(1).toString();
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();

			if(!strSymbols.isEmpty() && !strSymbols.contains(","))
			{
				ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbols);
				lstSymbols.add(atSymbol);
			}
			else
			{
				StringTokenizer symbolTokenizer = new StringTokenizer(strSymbols, ",");
				while(symbolTokenizer.hasMoreTokens())
				{
					ATSYMBOL atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken());
					lstSymbols.add(atSymbol);
				}
			}			
			List<ATQuoteFieldType> lstFieldTypes = new ArrayList<ATQuoteFieldType>();
			ATServerAPIDefines atServerAPIDefines = new ATServerAPIDefines();
			lstFieldTypes.add(atServerAPIDefines.new ATQuoteFieldType(ATQuoteFieldType.LastPrice));
			lstFieldTypes.add(atServerAPIDefines.new ATQuoteFieldType(ATQuoteFieldType.Volume));
			lstFieldTypes.add(atServerAPIDefines.new ATQuoteFieldType(ATQuoteFieldType.LastTradeDateTime));
			lstFieldTypes.add(atServerAPIDefines.new ATQuoteFieldType(ATQuoteFieldType.ProfileShortName));

			long request = apiSession.GetRequestor().SendATQuoteDbRequest(lstSymbols, lstFieldTypes, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbols + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		/**********************************************************************
		 * //getOptionChain
		 * Examples:
		 * 		getOptionChain AAPL
		 **********************************************************************/
		else if(count == 2 && ((String)ls.get(0)).equalsIgnoreCase("getOptionChain"))
		{
			String strSymbol = ls.get(1).toString();
			ATConstituentListType constituentType = (new ATServerAPIDefines()).new ATConstituentListType(ATConstituentListType.ConstituentListOptionChain);
			
			long request = apiSession.GetRequestor().SendATConstituentListRequest(constituentType, strSymbol.getBytes(), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);  

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}

		}
		
		/**********************************************************************
		 * 1 - REGULAR
		 * //getIntradayHistoryBars
		 * Examples:
		 * 		getIntradayHistoryBars CSCO 1 20100924130000 20100924130500
		 * 		getDailyHistoryBars CSCO 20100801100000 20100924130500
		 * 		getWeeklyHistoryBars CSCO 20100801100000 20100924130500
		 **********************************************************************/
		else if(count >= 5 && ((String)ls.get(0)).equalsIgnoreCase("getIntradayHistoryBars"))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			short minutes = Short.parseShort(ls.get(2).toString());
			
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(3).toString());
			SYSTEMTIME endDateTime = Helpers.StringToATTime(ls.get(4).toString());
			
			ATBarHistoryType barHistoryType = (new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryIntraday);
			long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(atSymbol, barHistoryType,
					minutes, beginDateTime, endDateTime, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}	
		
		/**********************************************************************
		 * 3
		 * //getIntradayHistoryBars
		 * Examples:
		 * 		getIntradayHistoryBars CSCO 1 20100924130000 20100924130500
		 * 		getDailyHistoryBars CSCO 20100801100000 20100924130500
		 * 		getWeeklyHistoryBars CSCO 20100801100000 20100924130500
		 **********************************************************************/
		else if(count == 6 && ((String)ls.get(0)).equalsIgnoreCase("getIntradayHistoryBars"))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			short minutes = Short.parseShort(ls.get(2).toString());
			
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(3).toString());
			int numRecords = Integer.parseInt(ls.get(4).toString());
			byte byteCursorType = (byte)Integer.parseInt(ls.get(5).toString());
			
			ATBarHistoryType barHistoryType = (new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryIntraday);
			long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(atSymbol, barHistoryType, minutes, beginDateTime, numRecords, 
					(new ATServerAPIDefines()).new  ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}	

		
		
		/**********************************************************************
		 * //1 - REGULAR 
		 * //getDailyHistoryBars | getWeeklyHistoryBars
		 * Example:
		 * 		getDailyHistoryBars CSCO 20100801100000 20100924130500
		 **********************************************************************/
		else if(count == 4 && ( ((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars") ||
								((String)ls.get(0)).equalsIgnoreCase("getWeeklyHistoryBars")))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(2).toString());
			SYSTEMTIME endDateTime = Helpers.StringToATTime(ls.get(3).toString());
			ATBarHistoryType barHistoryType = (((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars")) ? (new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) :
				(new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryWeekly);
										
			long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(atSymbol, barHistoryType, 
					(short)0, beginDateTime, endDateTime, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}		

		/**********************************************************************
		 * //2 - 
		 * //getDailyHistoryBars | getWeeklyHistoryBars
		 * Example:
		 * 		getDailyHistoryBars CSCO 5
		 **********************************************************************/
		else if(count == 3 && ( ((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars") ||
								((String)ls.get(0)).equalsIgnoreCase("getWeeklyHistoryBars")))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			int numRecords = Integer.parseInt(ls.get(2).toString());
			ATBarHistoryType barHistoryType = (((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars")) ? (new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) :
				(new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryWeekly);
										
			long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(atSymbol, barHistoryType, (short)0, 
					numRecords, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}		

		/**********************************************************************
		 * //3 - 
		 * //getDailyHistoryBars | getWeeklyHistoryBars
		 * Example:
		 * 		getDailyHistoryBars CSCO 20100601100000 500 1
		 **********************************************************************/
		else if(count == 5 && ( ((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars") ||
								((String)ls.get(0)).equalsIgnoreCase("getWeeklyHistoryBars")))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(2).toString());
			int numRecords = Integer.parseInt(ls.get(3).toString());
			byte byteCursorType = (byte)Integer.parseInt(ls.get(4).toString());
			ATBarHistoryType barHistoryType = (((String)ls.get(0)).equalsIgnoreCase("getDailyHistoryBars")) ? (new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) :
				(new ATServerAPIDefines()).new ATBarHistoryType(ATBarHistoryType.BarHistoryWeekly);
										
			long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(atSymbol, barHistoryType, (short)0, beginDateTime, numRecords, 
					(new ATServerAPIDefines()).new  ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}		
		
		/**********************************************************************
		 * 1 - REGULAR
		 * //Ticks
		 * Example:
		 * 		getTicks CSCO 20100924131112 20100924134012
		 **********************************************************************/
		else if(count == 4 && ((String)ls.get(0)).equalsIgnoreCase("getTicks"))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(2).toString());
			SYSTEMTIME endDateTime = Helpers.StringToATTime(ls.get(3).toString());
									
			long request = apiSession.GetRequestor().SendATTickHistoryDbRequest(atSymbol, true, true, beginDateTime, endDateTime, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}

		/**********************************************************************
		 * 2 - 
		 * //Ticks
		 * Example: 
		 * 		getTicks CSCO 100
		 **********************************************************************/
		else if(count == 3 && ((String)ls.get(0)).equalsIgnoreCase("getTicks"))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			
			int numRecords = Integer.parseInt(ls.get(2).toString());
									
			long request = apiSession.GetRequestor().SendATTickHistoryDbRequest(atSymbol, true, true, numRecords, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}


		/**********************************************************************
		 * 3 - 
		 * //Ticks
		 * Example:100 records going foward from given time
		 * 		getTicks CSCO 20100924131112 100 1
		 **********************************************************************/
		else if(count == 5 && ((String)ls.get(0)).equalsIgnoreCase("getTicks"))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			
			SYSTEMTIME beginDateTime = Helpers.StringToATTime(ls.get(2).toString());
			int numRecords = Integer.parseInt(ls.get(3).toString());
			byte byteCursorType = (byte)Integer.parseInt(ls.get(4).toString());
									
			long request = apiSession.GetRequestor().SendATTickHistoryDbRequest(atSymbol, true, true, beginDateTime, numRecords, 
					(new ATServerAPIDefines()).new  ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}

		
		
		/**********************************************************************
		 * //MarketMovers
		 * Symbols: NG - NetGainers, NL - NetLoosers, PG - PercentGainers, PL - PercentLoosers, VL - Volume
		 * Exchange: A - Amex, U - OTCBB, N - NyseEuronext, Q - NasdaqOmx
		 * 
		 * Example:
		 * 		getMarketMovers NG Q
		 **********************************************************************/
		else if(count >= 3 && ((String)ls.get(0)).equalsIgnoreCase("getMarketMovers"))
		{
			String strSymbol = ls.get(1).toString();

			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			atSymbol.symbolType = ATSymbolType.TopMarketMovers;
			atSymbol.exchangeType = (byte)ls.get(2).toString().getBytes()[0];
			
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();
			lstSymbols.add(atSymbol);
			long request = apiSession.GetRequestor().SendATMarketMoversDbRequest(lstSymbols, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		
		/**********************************************************************
		 * //subscribeMarketMovers | unsubscribeMarketMovers
		 * Example: subscribing/unsubscribing to Net-Gainers on Nasdaq exchange ( NG Q):
		 * 		subscribeMarketMovers NG Q
		 * 		unsubscribeMarketMovers NG Q
		 **********************************************************************/

		else if(count >= 2 && ( ((String)ls.get(0)).equalsIgnoreCase("subscribeMarketMovers") ||
								((String)ls.get(0)).equalsIgnoreCase("unsubscribeMarketMovers")))
		{
			String strSymbol = ls.get(1).toString();
			ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbol);
			atSymbol.symbolType = ATSymbolType.TopMarketMovers;
			atSymbol.exchangeType = (byte)ls.get(2).toString().getBytes()[0];
				
			ATStreamRequestType requestType = (new ATServerAPIDefines()).new ATStreamRequestType();
			requestType.m_streamRequestType = ((String)ls.get(0)).equalsIgnoreCase("subscribeMarketMovers") ? 
												ATStreamRequestType.StreamRequestSubscribe : ATStreamRequestType.StreamRequestUnsubscribe;
			
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();
			lstSymbols.add(atSymbol);
									
			long request = apiSession.GetRequestor().SendATMarketMoversStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbol + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		
		/**********************************************************************
		 * //subscribeQuoteStream | unsubscribeQuoteStream
		 * Example:
		 * Single symbol request:
		 * 		subscribeQuoteStream  AAPL
		 * 		unsubscribeQuoteStream  AAPL
		 * Multiple symbol request:
		 * 		subscribeQuoteStream  AAPL,AMZN
		 * 		unsubscribeQuoteStream  AAPL,AMZN
		 **********************************************************************/
		else if(count >= 2 && ( ((String)ls.get(0)).equalsIgnoreCase("subscribeQuoteStream") ||
				((String)ls.get(0)).equalsIgnoreCase("unsubscribeQuoteStream")))
		{
			String strSymbols = ls.get(1).toString();
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();

			if(!strSymbols.isEmpty() && !strSymbols.contains(","))
			{
				ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbols);
				lstSymbols.add(atSymbol);
			}
			else
			{
				StringTokenizer symbolTokenizer = new StringTokenizer(strSymbols, ",");
				while(symbolTokenizer.hasMoreTokens())
				{
					ATSYMBOL atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken());
					lstSymbols.add(atSymbol);
				}
			}			
			
			ATStreamRequestType requestType = (new ATServerAPIDefines()).new ATStreamRequestType();
			requestType.m_streamRequestType = ((String)ls.get(0)).equalsIgnoreCase("subscribeQuoteStream") ? 
											ATStreamRequestType.StreamRequestSubscribe : ATStreamRequestType.StreamRequestUnsubscribe;
			
			long request = apiSession.GetRequestor().SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbols + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		
		/**********************************************************************
		 * //subscribeQuotesOnlyQuoteStream | unsubscribeQuotesOnlyQuoteStream
		 * Example:
		 * Single symbol request:
		 * 		StreamRequestSubscribeQuotesOnly  AAPL
		 * 		StreamRequestUnsubscribeQuotesOnly  AAPL
		 * Multiple symbol request:
		 * 		StreamRequestSubscribeQuotesOnly  AAPL,AMZN
		 * 		StreamRequestUnsubscribeQuotesOnly  AAPL,AMZN
		 **********************************************************************/
		else if(count >= 2 && ( ((String)ls.get(0)).equalsIgnoreCase("subscribeQuotesOnlyQuoteStream") ||
				((String)ls.get(0)).equalsIgnoreCase("unsubscribeQuotesOnlyQuoteStream")))
		{
			String strSymbols = ls.get(1).toString();
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();

			if(!strSymbols.isEmpty() && !strSymbols.contains(","))
			{
				ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbols);
				lstSymbols.add(atSymbol);
			}
			else
			{
				StringTokenizer symbolTokenizer = new StringTokenizer(strSymbols, ",");
				while(symbolTokenizer.hasMoreTokens())
				{
					ATSYMBOL atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken());
					lstSymbols.add(atSymbol);
				}
			}			
			
			ATStreamRequestType requestType = (new ATServerAPIDefines()).new ATStreamRequestType();
			requestType.m_streamRequestType = ((String)ls.get(0)).equalsIgnoreCase("subscribeQuotesOnlyQuoteStream") ? 
											ATStreamRequestType.StreamRequestSubscribeQuotesOnly : ATStreamRequestType.StreamRequestUnsubscribeQuotesOnly;
			
			long request = apiSession.GetRequestor().SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbols + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		
		/**********************************************************************
		 * //subscribeTradesOnlyQuoteStream | unsubscribeTradesOnlyQuoteStream
		 * Example:
		 * Single symbol request:
		 * 		subscribeTradesOnlyQuoteStream  AAPL
		 * 		unsubscribeTradesOnlyQuoteStream  AAPL
		 * Multiple symbol request:
		 * 		subscribeTradesOnlyQuoteStream  AAPL,AMZN
		 * 		unsubscribeTradesOnlyQuoteStream  AAPL,AMZN
		 **********************************************************************/
		else if(count >= 2 && ( ((String)ls.get(0)).equalsIgnoreCase("subscribeTradesOnlyQuoteStream") ||
				((String)ls.get(0)).equalsIgnoreCase("unsubscribeTradesOnlyQuoteStream")))
		{
			String strSymbols = ls.get(1).toString();
			List<ATSYMBOL> lstSymbols = new ArrayList<ATSYMBOL>();

			if(!strSymbols.isEmpty() && !strSymbols.contains(","))
			{
				ATSYMBOL atSymbol = Helpers.StringToSymbol(strSymbols);
				lstSymbols.add(atSymbol);
			}
			else
			{
				StringTokenizer symbolTokenizer = new StringTokenizer(strSymbols, ",");
				while(symbolTokenizer.hasMoreTokens())
				{
					ATSYMBOL atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken());
					lstSymbols.add(atSymbol);
				}
			}			
			
			ATStreamRequestType requestType = (new ATServerAPIDefines()).new ATStreamRequestType();
			requestType.m_streamRequestType = ((String)ls.get(0)).equalsIgnoreCase("subscribeTradesOnlyQuoteStream") ? 
											ATStreamRequestType.StreamRequestSubscribeTradesOnly : ATStreamRequestType.StreamRequestUnsubscribeTradesOnly;
			
			long request = apiSession.GetRequestor().SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);
			
			System.out.println("SEND " + request + ": " + ls.get(0).toString() + " request [" + strSymbols + "]");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}
		
		/**********************************************************************
		 * //MarketHolidays
		 * Example:
		 * Notes:
		 * 	-Currently not being used
		 **********************************************************************/
		else if(count >= 2 && ( ((String)ls.get(0)).equalsIgnoreCase("getMarketHolidays")))
		{
			short yearsGoingBack = Short.parseShort(ls.get(1).toString());
			short yearsGoingForward = Short.parseShort(ls.get(2).toString());
			
			long request = apiSession.GetRequestor().SendATMarketHolidaysRequest(yearsGoingBack, yearsGoingForward, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);

			System.out.println("SEND " + request + ":MarketHolidays request for " + yearsGoingBack + " years back and " + yearsGoingForward + " years forward");
			if(request < 0)
			{
				System.out.println("Error = " + Errors.GetStringFromError((int)request));
			}
		}			
	}

	public void run()
	{		
		  serverapi = new ActiveTickServerAPI();	      
	      apiSession = new APISession(serverapi);
	      serverapi.ATInitAPI();
	      
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	      
	      while(true)
	      {
		      try 
		      {
			         String line = br.readLine();
			         if(line.length() > 0)
			         {
			        	 if(line.startsWith("quit"))
			        		 break;
			        	 
			        	 processInput(line);
			         }
			  } 
		      catch (IOException e) 
			  {
		    	  System.out.println("IO error trying to read your input!");
			  }
	      }
	      
	      apiSession.UnInit();
	      serverapi.ATShutdownAPI();
	}
	
	public static void main(String args[])
	{
		ATServerAPIExample apiExample = new ATServerAPIExample();
	}
}
