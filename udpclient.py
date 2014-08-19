import socket
import sys
import random
import time


RECORD_DATA = {'A00':0.117,'A01':6.247, 'A02':124.9, 'A03':1.0, 'A04':1.0, 'A05':1.0,
'A06':1.0, 'A07':1.0,'A08':1.0, 'A09':1.0, 'A10':1.0, 'A11':1.0, 'A12':1.0, 'A13':-0.06,
'A14':-0.06,'P01':1.0, 'P02':1.0, 'P03':1.0, 'P04':1.0, 'P05':1.0, 'P06':1.0,
'K01':1333333.000, 'O01':1.0}


def send(host, port, frequency):
    update_data()
    data = print_header() + print_data()

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    sock.sendto(data, (host, port))

    print "Sent:    {}\n".format(data)

def update_data():
    RECORD_NUMBER += 1

    for k, v in RECORD_DATA.items():
        RECORD_DATA[k] = randomize(v)

def randomize(value):
    "Increase or decrease value by 50% or less"
    return value * (random.random() + 0.5)

def print_header():
    TM = time.strftime('%y%m%d%H%M%S')
    header = '#STA:000001,511;L:310;TM:'+TM+';D:0;T:01;C:90;'

    return header

def print_data():
    tuples = [str(k) + ':' + str(v) for k, v, in RECORD_DATA.items()]
    result = ';'.join(tuples)
    return result

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print "Didn't specify [host] [port] [frequency]. Using: localhost 6001 1"
        host = 'localhost'
        port = 6001
        freq = 1
    else:
        host = sys.argv[1]
        port = sys.argv[2]
        freq = sys.argv[3]

    while(1)
        send(host, port)
        time.sleep(freq) # Seconds
