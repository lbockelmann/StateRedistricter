# Shapefile to GeoJSON code provided by
# https://groups.google.com/forum/#!topic/geospatialpython/7bZnpHkD7ys

import shapefile
import json

reader = shapefile.Reader("Statewide.shp")
fields = reader.fields[1:]
field_names = [field[0] for field in fields]
buffer = []
for sr in reader.shapeRecords():
    atr = dict(zip(field_names, sr.record))
    geom = sr.shape.__geo_interface__
    buffer.append((dict(type="Feature", geometry=geom, properties=atr)))


gf = open(f'SC_zoom7_0.geojson', 'w')
gf.write(json.dumps({"type": "FeatureCollection",
                     "features": buffer}) + "\n")
gf.close()
