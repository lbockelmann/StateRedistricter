import csv
import json

keys = ['year', 'state', 'state_po', 'state_fips', 'state_cen', 'state_ic', 'office', 'district',
        'stage', 'special', 'candidate', 'party', 'writein', 'candidatevotes', 'totalvotes', 'version']
keys = dict(list(zip(keys, range(len(keys)))))

f = open('1976-2016-house.csv', encoding='utf-8')
cf = csv.reader(f)

nogo = ['AK', 'DE', 'MT', 'ND',
        'SD', 'VT', 'WY']

# by state then year then district data

lines = list(cf)[1:]


class Candidate:
    def __init__(self, name, votes):
        self.name = name
        self.votes = votes

    def __str__(self):
        return "Candidate(name={}, votes={})".format(self.name, self.votes)

    def __repr__(self):
        return self.__str__()


states = {}
for i in lines:
    s = i[keys['state_po']].upper()
    if s in states:
        states[s].append(i)
    else:
        states[s] = [i]

for (k, j) in states.items():
    years = {}
    for i in j:
        y = int(i[keys['year']]) + 1
        if (y < 1996):
            continue
        if y in years:
            years[y].append(i)
        else:
            years[y] = [i]
    states[k] = years

for (k1, state) in states.items():
    for (k2, year) in state.items():
        districts = {}
        for line in year:
            d = int(line[keys['district']])
            if d in districts:
                districts[d].append(line)
            else:
                districts[d] = [line]
        state[k2] = districts
    states[k1] = state

for (k1, state) in states.items():
    for (k2, year) in state.items():
        for (k3, district) in year.items():
            election = {'candidates': []}
            for candidate in district:
                name = candidate[keys['candidate']] or '<write in>'
                votesReceived = int(candidate[keys['candidatevotes']])
                party = candidate[keys['party']] or 'none'
                election['candidates'].append((name, votesReceived, party))
                election['totalVotes'] = int(candidate[keys['totalvotes']])
            election['winner'] = max(
                election['candidates'], key=lambda x: x[1])
            election['totalRepVotes'] = sum(map(
                lambda x: x[1], filter(lambda k: k[2] == 'republican', election['candidates'])))
            election['totalDemVotes'] = sum(map(
                lambda x: x[1], filter(lambda k: k[2] == 'democrat', election['candidates'])))
            year[k3] = election
        state[k2] = year
    states[k1] = state

for (k, v) in states.items():
    if k not in nogo:
        with open('{}.eld'.format(k), 'w', encoding='utf-8') as jf:
            for (year, district_data) in v.items():
                jf.write('year:%d\n' % year)
                for (district_id, candidate_data) in district_data.items():
                    jf.write('district:%d\n' % district_id)
                    for (candidate) in candidate_data['candidates']:
                        name = candidate[0]
                        votes = candidate[1]
                        party = candidate[2]
                        jf.write(u'{},{},{}\n'.format(name, votes, party))
                    jf.write('district_end\n')
                jf.write('year_end\n')


# the javascript side of things requires the JSON data format
# so until we sort that out we need both versions of the file
# the java processable one (.eld) and the javascript one (.json)
for (k, v) in states.items():
    if k not in nogo:
        with open('{}.json'.format(k), 'w') as jf:
            json.dump(v, jf, indent=2)
