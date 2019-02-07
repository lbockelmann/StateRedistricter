import argparse
import csv
import json
import os.path

import shapely.geometry

COUNTY_IDX = 3
POPULATION_IDX = 9

maps_root = os.path.realpath(os.path.join('..', 'js', 'Maps'))

years = [1997, 1999, 2001, 2003, 2005, 2007, 2009, 2017]
states = ['UT', 'NM', 'SC']


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--state', help='State abbreviation to process')
    return parser.parse_args()


def estimate_precinct_populations(state, year):
    with open(f'{state}/{state}_pop.csv') as cf:
        reader = csv.reader(cf)
        # discard keys
        next(reader)
        pops = {}  # by county id
        for row in reader:
            pops[row[COUNTY_IDX]] = row[POPULATION_IDX]

    counties = {}  # by id
    with open(f'{state}/{state}_counties.geojson') as jf:
        data = json.load(jf)
        for feature in data['features']:
            cid = feature['properties']['COUNTYFP10']
            counties[cid] = shapely.geometry.shape(feature['geometry'])

    precincts = {}
    pdata = {}
    with open(os.path.join(maps_root, state, str(2017), f'{state}_zoom7_0.geojson')) as jf:
        data = json.load(jf)
        for precinct in data['features']:
            try:
                shape = shapely.geometry.shape(precinct['geometry'])
                precincts[precinct['properties']['id']] = shape.centroid
                pdata[precinct['properties']['id']] = precinct
            except KeyError:
                print(precinct)
                exit(1)

    containers = {k: [] for k in counties.keys()}

    for (pid, pcenter) in precincts.items():
        for (cid, cshape) in counties.items():
            if cshape.contains(pcenter):
                containers[cid].append(pid)

    avgsize = {}
    for (cid, plist) in containers.items():
        cpop = pops[cid]
        print(f'[#] Average size for precincts in county[{cid}] is {int(cpop)/len(plist)}')
        for i in plist:
            avgsize[i] = int(cpop) / len(plist)

    for (pid, precinct) in pdata.items():
        for (opid, size) in avgsize.items():
            if pid == opid:
                pdata[pid]['properties']['population'] = str(int(avgsize[opid]))

    with open(os.path.join(maps_root, state, str(year), f'{state}_zoom7_0.geojson'), 'w') as jf:
        for (pid, precinct) in pdata.items():
            if year < 2017:
                del precinct['geometry']
        obj = {'type': 'FeatureCollection', 'features': list(pdata.values())}
        json.dump(obj, jf)

    with open(f'{state}/{state}Population.csv', 'w') as jf:
        jf.write('precinct_id,population\n')
        for (pid, size) in avgsize.items():
            jf.write(f'{pid},{str(int(size))}\n')


def main():
    options = parse_args()
    for s in states:
        print(f'[+] Processing state {s}')
        for y in years:
            print(f'[+] Processing year {y}')
            estimate_precinct_populations(s, y)


main()
