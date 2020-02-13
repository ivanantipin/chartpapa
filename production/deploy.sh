../gradlew clean build installDist

target_dir=$1

if [ -n "$1" ]; then
  echo "target path is $1"
else
  echo "you need to specify target dir"
  exit -1
fi

rm -rfv $target_dir/bin
rm -rfv $target_dir/lib
rm -rfv $target_dir/market_research

cp -rv build/install/production $target_dir

cp -rv ../market_research $target_dir/
