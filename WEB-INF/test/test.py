import requests
import json
import time

values = json.load(open('results2.json'))


def init():
    print('Initializing view...')
    r = requests.get('http://localhost:8080/CSE308')
    print(r)
    print('Loading states...')
    requests.get('http://localhost:8080/CSE308/index', params={'state': 'all'})
    print(r)


def loadState(abbr):
    print("Loading state {}...".format(abbr))
    r = requests.get('http://localhost:8080/CSE308/index',
                     params={'state': abbr})
    print(r)


def test_loading(abbr):
    init()
    loadState(abbr)


def test_weights(abbr, weights, testname):
    init()
    loadState(abbr)
    print('Testing weights for {}'.format(abbr))
    r = requests.post('http://localhost:8080/CSE308/start_algo', json=weights)
    print(r)
    if r.status_code == 200:
        lowest = float('inf')
        highest = float('-inf')
        first = None
        last = None
        total = 0
        count = 0
        going = True
        while going:
            q = requests.get('http://localhost:8080/CSE308/start_algo',
                             params={'cont': 1})
            data = q.json()
            going = data['algorithmDone'] == 'false'
            for i in data['moves']:
                v = float(i['functionValue'])
                if first is None:
                    first = v
                if v > highest:
                    highest = v
                if v < lowest:
                    lowest = v
                total += v
                count += 1
                last = v
        values[testname] = {}
        values[testname]['highest'] = highest
        values[testname]['lowest'] = lowest
        values[testname]['average'] = total / count
        values[testname]['first'] = first
        values[testname]['last'] = last


def zeroWeights(w):
    w['compactness'] = 0
    w['efficiencyGap'] = 0
    w['partisanFairness'] = 0
    w['populationEquality'] = 0
    return w


def test_all_weights():
    # states = ['NM', 'SC', 'UT']
    # for i in states:
    weights = {
        'compactness': 0.45,
        'efficiencyGap': 0.1,
        'partisanFairness': 0,
        'populationEquality': 0.2,
        'numSeedDistricts': 3,
        'approach': 'AWLA',
        'stateAbbr': 'UT',
        'year': 2017
    }

    for i in range(3):
        test_weights('UT', weights, 'UT-AWLA-MULTIPLE_WEIGHTS_10_{}'.format(i))

    with open('results2.json', 'w') as rf:
        json.dump(values, rf)


test_all_weights()
