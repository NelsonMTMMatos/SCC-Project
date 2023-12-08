#!/bin/bash

# Run the first script to seed data
echo "Starting data seeding..."
artillery run load-data.yml
echo "Data seeding complete."

# Run the second script to test the workload
echo "Starting workload testing..."
artillery run --output run-01.json workload1.yml
echo "Workload testing complete."

artillery report run-01.json
