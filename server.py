# server.py

from flask import Flask, jsonify, request

app = Flask(__name__)
user_id=00000
position=11111
timestamp=22222

get_response = [
    { 'user_id': user_id, 'position': position, 'timestamp' : timestamp }
]

#routes
@app.route('/update_position', methods=['POST'])
def update_position():
    data = request.get_json()
    user_id= data['user']['id']
    #TODO
    #find user id in the db and update position
    return get_response

@app.route('/get_position', methods=['POST'])
def get_position():
    data = request.get_json()
    user_id= data['user']['id']
    #TODO
    #find user id in the db and return position
    return get_response

@app.route("/")
def hello_world():
    return "Hello!"