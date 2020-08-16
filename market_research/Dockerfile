FROM continuumio/anaconda3

RUN apt install -y --quiet build-essential \
&& /opt/conda/bin/conda install jupyter -y --quiet \
&& mkdir /opt/notebooks && conda install -c ranaroussi -y --quiet yfinance \
&& /opt/conda/bin/pip install PyPortfolioOpt

COPY 4-Black-Litterman-Allocation.ipynb /opt/notebooks

COPY targets.csv /opt/notebooks

CMD /opt/conda/bin/jupyter lab --notebook-dir=/opt/notebooks --ip='0.0.0.0' --port=8888 --no-browser --allow-root