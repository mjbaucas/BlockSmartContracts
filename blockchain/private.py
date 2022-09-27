import datetime
from block import Block

class Chain:
    def __init__(self):
        self.chain = [gen_genesis_block()]

    def gen_next_block(self, public_key, contracts):
        prev_block = self.chain[-1]
        index = prev_block.index + 1
        timestamp = datetime.datetime.now()
        data = contracts
        hashed_block = prev_block.gen_hashed_block()
        self.chain.append(Block(index, timestamp, [], hashed_block, data, public_key))

    def display_contents(self):
        for block in self.chain:
            block.disp_block_info()

    def output_ledger(self):
        main_transactions = []
        for block in self.chain:
            if block.index != 0:
                temp_transactions = block.get_block_transactions()
                for temp_transaction in temp_transactions:
                    main_transactions.append(temp_transaction)
        return main_transactions

    def search_ledger(self, key):
        for i in self.chain[::-1]:
            if(i.validate_private_key(key)):
                return i
        return None

    def search_smart_contracts(self, source, destination):
        for i in self.chain[::-1]:
            if i.search_contracts(source, destination):
                return True
        return False

def gen_genesis_block():
    transaction = ["XX:XX:XX:XX:XX"]
    return Block(0, datetime.datetime.now(), transaction, "0", [], "0")