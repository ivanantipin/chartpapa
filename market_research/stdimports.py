import pandas as pd
import requests
from lxml import etree
import sys
import importlib
import matplotlib.pyplot as plt
sys.path.insert(0,'..')
import utils
import finam.export as fe
importlib.reload(fe)
importlib.reload(utils)

import datetime
print(' last updated time is ' + str(datetime.datetime.now()))