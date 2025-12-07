import csv
import os
from elasticsearch import Elasticsearch
from elasticsearch.helpers import scan
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

FIELDS = [
    "id"
    "groupId",
    "name",
    "description",
    "startDate",
    "endDate",
    "formattedLocation",
    "countryDescription",
    "attendeesCount",
    "companyAttendeesCount",
    "investorAttendeesCount",
    "attendeeNamesConcatString",
    "industrySectorsConcatString",
    "industryGroupsConcatString",
    "industryCodesConcatString"
]

# Read required environment variables
SOURCE_ES_URL = os.getenv("SOURCE_ES_URL")
SOURCE_ES_USERNAME = os.getenv("SOURCE_ES_USERNAME")
SOURCE_ES_PASSWORD = os.getenv("SOURCE_ES_PASSWORD")
SOURCE_ES_INDEX_NAME = os.getenv("SOURCE_ES_INDEX_NAME")

# Optional: Output file name can be based on index name
TARGET_CSV_FILE = f"{SOURCE_ES_INDEX_NAME or 'es_index_data'}.csv"

def main():
    es = Elasticsearch(
        SOURCE_ES_URL,
        basic_auth=(SOURCE_ES_USERNAME, SOURCE_ES_PASSWORD),
        verify_certs=True
    )

    results = scan(
        es,
        index=SOURCE_ES_INDEX_NAME,
        query={"query": {"match_all": {}}},
        preserve_order=True
    )

    seen_ids = set()
    duplicate_count = 0
    with open(TARGET_CSV_FILE, mode="w", newline="", encoding="utf-8") as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=FIELDS)
        writer.writeheader()
        for doc in results:
            source = doc.get("_source", {})
            doc_id = source.get("id")
            if doc_id in seen_ids:
                duplicate_count += 1
                continue  # Skip duplicate
            seen_ids.add(doc_id)
            row = {field: source.get(field, "") for field in FIELDS}
            writer.writerow(row)
    print(f"Total duplicates skipped: {duplicate_count}")
    print(f"Total unique documents written: {len(seen_ids)}")

if __name__ == "__main__":
    main()
