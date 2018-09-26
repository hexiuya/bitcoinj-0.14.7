docker stop bitcoin-wallet
docker rm bitcoin-wallet
docker image rm bitcoin-wallet:0.0.1
docker build . -t bitcoin-wallet:0.0.1
docker run -d -p 8882:8882 -v /home/xiuya/UAT/bitcoinj-0.14.7/bitcoin-wallet/data:/data --name bitcoin-wallet --network crm-network --network-alias alias-bitcoin-wallet --link crm-test-mysql bitcoin-wallet:0.0.1
