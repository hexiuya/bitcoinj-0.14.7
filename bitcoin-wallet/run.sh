Xvfb -ac :7 -screen 0 1024x768x16 &> xvfb.log &
java -jar bitcoin-wallet-0.14.7.jar 
