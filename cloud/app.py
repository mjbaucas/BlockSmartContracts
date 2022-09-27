from flask import Flask, render_template, request
import json
import time
import sys

app = Flask(__name__)

import sys
sys.path.append('../')
sys.path.append('../blockchain')

from blockchain.private import Chain as PrivateBlockChain

contract_length = 100

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
    counter+=1
    temp_blockchain.append({'source': 'placeholder', 'destination': 'placeholder', 'allow': False})
    temp_list.append({'source': 'placeholder', 'destination': 'placeholder', 'allow': False})

    if counter == 10:
        private_chain.gen_next_block("temp", temp_blockchain)
        counter = 0
        temp_blockchain = []

@app.route('/', methods=['GET'])
def get_info():
    return render_template('index.html', blockchain=blockchain)

@app.route('/blockchain/send', methods=['POST'])
def send_data_blockchain():
    response = request.get_json()

    found = False
    if private_chain.search_smart_contracts(response['source'], response['destination']):
        found = True

    return {"verified": found}, 200

@app.route('/list/request', methods=['POST'])
def request_access_list():
    response = request.get_json()

    found = False
    for item in temp_list[::-1]:
        if(item['source'] == response['source'] and item['destination'] == response['destination'] and item['allow'] == True):
            found = True

    return {"verified": found, "password": "temp"}, 200

@app.route('/list/send', methods=['POST'])
def send_data_list():
    response = request.get_json()

    found = False
    for item in temp_list[::-1]:
        if(item['source'] == response['source'] and item['destination'] == response['destination'] and item['allow'] == True and response['password'] == "temp"):
            found = True

    return {"verified": found}, 200

if __name__ == "__main__":
    app.run(host='0.0.0.0', threaded=True, port=3000)