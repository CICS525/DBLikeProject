'''
Please install azure package before use
http://azure.microsoft.com/en-us/documentation/articles/python-how-to-install/
'''

from azure.storage import TableService
from azure.storage import BlobService

ELI_STORAGE_ACCOUNT = "portalvhdsql3h2lbtq12d7"
ELI_STORAGE_KEY = "uC6oYc8BafbOaFme6dZp5MKgZUQrDk+wAz0vCf7ISC1JHDolgwIYxlHuKgAXWseRxMNlHpqjNRgtw90qE7wvzA=="

CHRIS_STORAGE_ACCOUNT = "portalvhds96n2s1jyj5b5k"
CHRIS_STORAGE_KEY = "vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA=="

SKY_STORAGE_ACCOUNT = "portalvhds0c37fqp3tw964"
SKY_STORAGE_KEY = "n8uEGZrIUoMcD4J7WgbcZyk6gMZ0hV9mtn83jXtMpWwLjFAWlPSZizDdZiWmeLjJMetOvrMko1dwoQnaUQTSLQ=="


ACCOUNT = [ELI_STORAGE_ACCOUNT, CHRIS_STORAGE_ACCOUNT, SKY_STORAGE_ACCOUNT]
KEY = [ELI_STORAGE_KEY, CHRIS_STORAGE_KEY, SKY_STORAGE_KEY]

BLOB_CONTAINERS = ["bolbpool"]

TABLES = ["account", "meta"]

def delete_blobs():
    print "deleting blobs: " + ", ".join(BLOB_CONTAINERS)
    for i in range(len(ACCOUNT)):
        bs = BlobService(ACCOUNT[i], KEY[i])
        for container in BLOB_CONTAINERS:
            bs.delete_container(container)

def delete_tables():
    print "deleting tables: " + ", ".join(TABLES)
    for i in range(len(ACCOUNT)):
        ts = TableService(ACCOUNT[i], KEY[i])
        for table in TABLES:
            ts.delete_table(table)

if __name__ == '__main__':
    print "start"
    delete_blobs()
    delete_tables()
    print "finished"
