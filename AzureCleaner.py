'''
Please install azure package before use
http://azure.microsoft.com/en-us/documentation/articles/python-how-to-install/
'''

from azure.storage import TableService
from azure.storage import BlobService

ELI_STORAGE_ACCOUNT = "cloudsync"
ELI_STORAGE_KEY = "JAOg0vyETxNJDgadihZCLDJzjqgJ79eGWbgbX6pIkAnXTIVqc1oxd+cH6caU9kB5lLhwrXKRaaAD/ak+raq4tg=="

CHRIS_STORAGE_ACCOUNT = "portalvhds049kfr2ss7hpd"
CHRIS_STORAGE_KEY = "w9StAFFrwJ7kFkOmiWLB/nH/rR1HUIVJhan4N5H6YZEgl9BnBtF8BRK5xMo6KLZ+UavOoAza7bzkfjSziSQcWw=="

SKY_STORAGE_ACCOUNT = "portalvhds98y1bsjj9fbb7"
SKY_STORAGE_KEY = "qfs8hait+aDHDFyNG/I4GzWeVjsmYp7aI2H1G4yCE67YY2XFSp6F1P/OO/qq7IIqBnHRgwbOSjre/xPbTmzT9Q=="


ACCOUNT = [ELI_STORAGE_ACCOUNT, CHRIS_STORAGE_ACCOUNT, SKY_STORAGE_ACCOUNT]
KEY = [ELI_STORAGE_KEY, CHRIS_STORAGE_KEY, SKY_STORAGE_KEY]

BLOB_CONTAINERS = ["bolbpool"]

TABLES = ["account", "meta", "masterblob"]

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
