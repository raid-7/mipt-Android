#!/usr/bin/env bash

for ((i = 0 ; i < $1 ; i++)); do
  curl -L -o "p${i}.jpg" -A "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36" https://thispersondoesnotexist.com/image
done
