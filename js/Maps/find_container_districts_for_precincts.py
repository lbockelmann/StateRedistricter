import argparse
import json
import os.path

import shapely.geometry

# geo.shape objects have intersects if you import geo from shapely.geometry

# shape preprocessing to file ? and then have java (faster) load it and work with it
# rather than calculating it?

sep = os.path.sep


def parse_args():
    ap = argparse.ArgumentParser()
    ap.add_argument('-s', '--state',
                    help='Two letter abbreviation of state to operate on')
    return ap.parse_args()


def calculate_districts(state_abbr, year):
    with open(f'{state_abbr.upper()}{sep}{state_abbr}_zoom7_0.geojson') as f:
        precincts = json.load(f)

    with open(f'{state_abbr.upper()}{sep}{year}{sep}{state_abbr}_{year}_D.geojson') as f:
        districts = json.load(f)

    idx = 0
    ps = {}
    for precinct in precincts['features']:
        precinct['properties']['id'] = 'p' + str(idx)
        idx += 1
        ps[precinct['properties']['id']] = precinct
    idx = 1
    ds = {}
    for district in districts['features']:
        try:
            district['properties']['id'] = 'd' + str(idx)
        except KeyError:
            district['properties'] = {}
            district['properties']['id'] = 'd' + str(idx)
        # do not change the existing district ids
        idx += 1
        ds[district['properties']['id']] = district

    p_centers = {}
    for precinct in precincts['features']:
        shape = shapely.geometry.shape(precinct['geometry'])
        p_centers[precinct['properties']['id']] = shape

    dshapes = {}
    for district in districts['features']:
        shape = shapely.geometry.shape(district['geometry'])
        dshapes[district['properties']['id']] = shape

    for (precinct_id, pshape) in p_centers.items():
        for (district_id, shape) in dshapes.items():
            if shape.contains(pshape.centroid):
                try:
                    del ps[precinct_id]['geometry']
                except KeyError:
                    print('Error' + str(ps[precinct_id]))
                # try:
                #     del ps[precinct_id]['containing_district_id']
                # except KeyError:
                #     print('No containing district id')
                #     pass
                ps[precinct_id]['properties']['containing_district_id'] = district_id
                ps[precinct_id]['properties']['center_point'] = [
                    pshape.centroid.x, pshape.centroid.y]
                print("Found district for precinct {}".format(precinct_id))

    precincts = list(ps.values())
    districts = list(ds.values())

    with open(f'{state_abbr.upper()}{sep}{year}{sep}{state_abbr}_zoom7_0.geojson', 'w') as f:
        obj = {'type': 'FeatureCollection', 'features': precincts}
        json.dump(obj, f)

    with open(f'{state_abbr.upper()}{sep}{year}{sep}{state_abbr}_{year}_D.geojson', 'w') as f:
        obj = {'type': 'FeatureCollection', 'features': districts}
        json.dump(obj, f)


years = list(reversed([2013, 2015]))


def main():
    options = parse_args()
    for state in ['UT', 'SC', 'NM']:
        print('finding for ' + state)
        for year in years:
            print('finding for ' + str(year))
            calculate_districts(state, year)


if __name__ == '__main__':
    main()
