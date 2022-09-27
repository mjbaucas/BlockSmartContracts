import requests
import json
import datetime
import socket
from utils import send_msg, recv_msg

import sys
sys.path.append('../')
sys.path.append('../blockchain')

from blockchain.private import Chain as PrivateBlockChain

host_ip = sys.argv[1]
port = int(sys.argv[2])
contract_length = int(sys.argv[3])
mode = int(sys.argv[4])
print('host ip: ', host_ip)

private_chain = PrivateBlockChain()
counter = 2
temp_blockchain = [
    {'source': 'galaxy', 'destination': 'cloud', 'allow': True},
    {'source': 'fossil6', 'destination': 'cloud', 'allow': True}
]
temp_list = [
    {'source': 'galaxy', 'destination': 'cloud', 'allow': True},
    {'source': 'fossil6', 'destination': 'cloud', 'allow': True}
]

for x in range(0, int(contract_length)):
    temp_blockchain.append({'source': 'placeholder', 'destination': 'placeholder', 'allow': False})
    temp_list.append({'source': 'placeholder', 'destination': 'placeholder', 'allow': False})
    counter+=1
    
    if counter == 10:
        private_chain.gen_next_block("temp", temp_blockchain)
        counter = 0
        temp_blockchain = []

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((host_ip, port))
s.listen(10)
total = 0
counter = 0

blockchain_url = 'http://192.168.2.25:3000/blockchain/send'
list_verify_url = 'http://192.168.2.25:3000/list/request'
list_send_url = 'http://192.168.2.25:3000/list/send'
while True:
    conn, addr = s.accept()
    print('Connected by', addr)
    data = recv_msg(conn)
    if not data:
        break
    print(data)
    message = data.decode().split("_")

    delta = 0.0
    start = datetime.datetime.now()
    if mode == 0:
        if private_chain.search_smart_contracts(message[0], message[1]):
            data = {'source': message[0], 'destination': message[1], 'data': 'normal'}
            x = requests.post(blockchain_url, json=data, headers={'Content-Type': 'application/json', 'X-Api-Key' : ''})
            elapsed = x.elapsed.total_seconds()
            print(x.text)
            print(elapsed)
            end = datetime.datetime.now()
            delta = int((end-start).total_seconds()*1000)
            total+=delta
            counter+=1
    elif mode == 1:
        for item in temp_list[::-1]:
            if(item['source'] == message[0] and item['destination'] == message[1] and item['allow'] == True):
                data = {'source': message[0], 'destination': message[1]}
                x = requests.post(list_verify_url, json=data, headers={'Content-Type': 'application/json', 'X-Api-Key' : ''})
                elapsed_request = x.elapsed.total_seconds()
                response = json.loads(x.text)
                if response["verified"] == True and response["password"]:
                    password = response["password"]
                    data = {'source': message[0], 'destination': message[1], 'password': password, 'data': 'normal'}
                    y = requests.post(list_send_url, json=data, headers={'Content-Type': 'application/json', 'X-Api-Key' : ''})
                    elapsed_send= y.elapsed.total_seconds()
                print(y.text)
                print(elapsed_send + elapsed_request)
                end = datetime.datetime.now()
                delta = int((end-start).total_seconds()*1000)
                total+=delta
                counter+=1
                
    msg = str(delta)
    send_msg(conn, bytearray(msg.encode()))
    print('Result:' + msg )
    print("Average Time: " + str(total/counter))  
    conn.close()