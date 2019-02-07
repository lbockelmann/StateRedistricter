import json

from shapely.geometry import geo
import shapely.errors
import os
import argparse


sep = os.path.sep


def parse_args():
    ap = argparse.ArgumentParser()
    ap.add_argument('-s', '--state',
                    help='Two letter abbreviation of state to operate on')
    return ap.parse_args()


def calculate_neighbors(state_abbr, year):
    with open(f'{state_abbr.upper()}{sep}2017{sep}{state_abbr}_zoom7_0.geojson') as f:
        precincts = json.load(f)

    pshapes = {}
    for pfeature in precincts['features']:
        pshapes[pfeature['properties']['id']] = geo.shape(pfeature['geometry'])

    all_neighbors = {}

    # create a dictionary of precinct id keys and lists of ids of neighbors
    # will be saved in a separate file not with GeoJSON since this data is
    # not stored in the master DB it will have to be transmitted separately
    # so keeping it separate will be helpful
    for (outer_id, shape) in pshapes.items():
        print(f"Finding neighbors for {outer_id}")
        for (inner_id, candidate) in pshapes.items():
            # note that touches alone is not enough
            try:
                if (shape.touches(candidate) or shape.intersects(candidate) or
                        shape.overlaps(candidate)):
                    if outer_id in all_neighbors:
                        all_neighbors[outer_id].append(inner_id)
                    else:
                        all_neighbors[outer_id] = [inner_id]
            except shapely.errors.TopologicalError as e:
                print(f'Error in finding neighbors for {outer_id}.')
                print(e)

    with open(f'{state_abbr.upper()}{sep}{year}{sep}{state_abbr}_p_neighbors.scsv', 'w') as results_file:
        for (pid, neighbor_list) in all_neighbors.items():
            results_file.write(f'{pid}|{",".join(neighbor_list)}\n')


def main():
    options = parse_args()
    for state in ['UT', 'SC', 'NM']:
        for year in [2011, 2013, 2015]:
            calculate_neighbors(state, year)


main()
