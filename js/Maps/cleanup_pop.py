import os

d = os.walk('.')

for i in d:
    for file in i[2]:
        if '_pop.geojson' in file:
            os.remove(os.path.join(i[0], file))
