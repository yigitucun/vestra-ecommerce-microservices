#!/bin/sh

echo "=========================================================="
echo " Debezium Auto-Registration Script Starting..."
echo "=========================================================="

DEBEZIUM_URL="${DEBEZIUM_URL:-http://debezium:8083}"

echo "Waiting for Debezium REST API to be available at $DEBEZIUM_URL..."
until curl -s -f "$DEBEZIUM_URL/connectors" > /dev/null 2>&1; do
  echo "Debezium not ready yet. Retrying in 3 seconds..."
  sleep 3
done

echo "Debezium is UP and READY!"

for file in /connectors/*.json; do
  [ -f "$file" ] || continue
  
  connector_name=$(grep -o '"name"[[:space:]]*:[[:space:]]*"[^"]*"' "$file" | head -1 | cut -d'"' -f4)
  
  if [ -z "$connector_name" ]; then
    echo "Warning: Could not parse connector name from $file, skipping."
    continue
  fi

  echo "----------------------------------------------------------"
  echo "Checking connector: $connector_name"

  # Substitute environment variable
  payload=$(sed "s|\${POSTGRES_PASSWORD}|$POSTGRES_PASSWORD|g" "$file")

  # Extract only config block for PUT update
  config_json=$(echo "$payload" | sed -n '/"config"/,$p' | sed '1s/.*"config"[[:space:]]*:[[:space:]]*{//' | sed '$s/}[[:space:]]*}//' | sed '1s/^/{/')

  # Check if already registered
  status_code=$(curl -s -o /dev/null -w "%{http_code}" "$DEBEZIUM_URL/connectors/$connector_name")

  if [ "$status_code" -eq 200 ]; then
    echo "Connector '$connector_name' already exists. Updating config..."
    curl -s -X PUT -H "Content-Type: application/json" -d "$config_json" "$DEBEZIUM_URL/connectors/$connector_name/config" > /dev/null
    echo "Connector '$connector_name' updated successfully."
  else
    echo "Registering new connector '$connector_name'..."
    response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$payload" "$DEBEZIUM_URL/connectors")
    http_code=$(echo "$response" | tail -n1)
    if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
      echo "Connector '$connector_name' registered successfully (HTTP $http_code)."
    else
      echo "Failed to register '$connector_name' (HTTP $http_code): $(echo "$response" | head -n -1)"
    fi
  fi
done

echo "=========================================================="
echo " All Debezium connectors processed successfully!"
echo "=========================================================="
exit 0
