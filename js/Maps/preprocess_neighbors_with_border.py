import argparse
import json
import os

import shapely.errors
from shapely.geometry import geo

sep = os.path.sep


class Precinct:
    def __init__(self, id, d, shape):
        self.id = id
        self.district_id = d
        self.neighbors = []
        self.shape = shape
        self.checked = False

    def __str__(self):
        return self.id


def parse_args():
    ap = argparse.ArgumentParser()
    ap.add_argument('-s', '--state',
                    help='Two letter abbreviation of state to operate on')
    return ap.parse_args()


def calculate_border_precincts(state_abbr, year):
    with open(f'{state_abbr.upper()}{sep}2017{sep}{state_abbr}_zoom7_0.geojson') as f:
        precincts = json.load(f)

    pshapes = {}
    for pfeature in precincts['features']:
        try:
            precinct = Precinct(pfeature['properties']['id'], pfeature['properties']['containing_district_id'], geo.shape(pfeature['geometry']))
            pshapes[pfeature['properties']['id']] = precinct
        except KeyError as e:
            id = pfeature['properties']['id'] if 'properties' in pfeature and 'id' in pfeature['properties'] else '?'
            print(f"Error on precinct: {id} -> {e}")

    all_neighbors = {}

    # dictionary relating district ids to lists of border precinct ids
    border_precincts = set(map(lambda p: p.district_id, pshapes.values()))
    border_precincts = {k: [] for k in border_precincts}

    # all neighbors is a dictionary that maps precinct objects to lists
    # of objects that are their neighbors
    for (outer_id, precinct) in pshapes.items():
        print(f"Finding neighbors for {outer_id}")
        for (inner_id, candidate_precinct) in pshapes.items():
            try:
                # note that touches alone is not enough
                if (precinct.shape.touches(candidate_precinct.shape) or
                        precinct.shape.intersects(candidate_precinct.shape) or
                        precinct.shape.overlaps(candidate_precinct.shape)):
                    if precinct in all_neighbors:
                        all_neighbors[precinct].append(candidate_precinct)
                    else:
                        all_neighbors[precinct] = [candidate_precinct]
            except shapely.errors.TopologicalError as e:
                print(f'Error in finding neighbors for {outer_id}.')
                print(e)

    # run through all the neighbors of each precinct and if any of them
    # are in a district that is not the same as the precinct we call that
    # a border precinct
    for (precinct, neighbors) in all_neighbors.items():
        if any(i.district_id != precinct.district_id for i in neighbors):
            # if this is true then this precinct `precinct` is a border
            # precinct within its district
            border_precincts[precinct.district_id].append(precinct)

    with open(f'{state_abbr.upper()}{sep}{year}{sep}{state_abbr}_border_precincts.scsv', 'w') as f:
        for (did, precincts) in border_precincts.items():
            f.write(f'{did}|{",".join(list(map(lambda p: p.id, precincts)))}\n')


def main():
    options = parse_args()
    for state in ['UT', 'SC', 'NM']:
        for year in [2011, 2013, 2015]:
            calculate_border_precincts(state, year)


main()
