import argparse
import json
# import shapely
import os.path

years = [1993, 1995, 1997, 1999, 2001, 2003, 2005, 2007, 2009]

abbreviations = {
    # 'Alabama': 'AL',
    # 'Alaska': 'AK',
    # 'Arizona': 'AZ',
    # 'Arkansas': 'AR',
    # 'California': 'CA',
    # 'Colorado': 'CO',
    # 'Connecticut': 'CT',
    # 'Delaware': 'DE',
    # 'District Of Columbia': 'DC',
    # 'Florida': 'FL',
    # 'Georgia': 'GA',
    # 'Hawaii': 'HI',
    # 'Idaho': 'ID',
    # 'Illinois': 'IL',
    # 'Indiana': 'IN',
    # 'Iowa': 'IA',
    # 'Kansas': 'KS',
    # 'Kentucky': 'KY',
    # 'Louisiana': 'LA',
    # 'Maine': 'ME',
    # 'Maryland': 'MD',
    # 'Massachusetts': 'MA',
    # 'Michigan': 'MI',
    # 'Minnesota': 'MN',
    # 'Mississippi': 'MS',
    # 'Missouri': 'MO',
    # 'Montana': 'MT',
    # 'Nebraska': 'NE',
    # 'Nevada': 'NV',
    # 'New Hampshire': 'NH',
    # 'New Jersey': 'NJ',
    # 'New Mexico': 'NM',
    # 'New York': 'NY',
    # 'North Carolina': 'NC',
    # 'North Dakota': 'ND',
    # 'Ohio': 'OH',
    # 'Oklahoma': 'OK',
    # 'Oregon': 'OR',
    # 'Pennsylvania': 'PA',
    # 'Rhode Island': 'RI',
    'South Carolina': 'SC',
    # 'South Dakota': 'SD',
    # 'Tennessee': 'TN',
    # 'Texas': 'TX',
    # 'Utah': 'UT',
    # 'Vermont': 'VT',
    # 'Virginia': 'VA',
    # 'Washington': 'WA',
    # 'West Virginia': 'WV',
    # 'Wisconsin': 'WI',
    # 'Wyoming': 'WY'
}


def parse_args():
    ap = argparse.ArgumentParser()
    ap.add_argument('-s', '--state',
                    help='Two letter abbreviation of state to operate on')
    ap.add_argument('-y', '--year', help='4 digit year of consolidation')
    return ap.parse_args()


def consolidate_district_files(state, year):
    root = os.path.join(state, str(year))
    features = {'type': 'FeatureCollection', 'features': []}
    for f in os.listdir(root):
        if 'D' not in f:
            with open(os.path.join(root, f)) as s:
                feature = json.load(s)
                features['features'].append(feature)
    with open(os.path.join(root, f'{state}_{year}_D.geojson'), 'w') as r:
        json.dump(features, r)


def main():
    options = parse_args()
    for state in abbreviations.values():
        try:
            print(f'Processing state {state}')
            for year in years:
                print(f'Processing year {year}')
                consolidate_district_files(state, year)
        except FileNotFoundError:
            print(f'Not processing {state}')

main()
