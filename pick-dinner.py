#!/usr/bin/env python3

import json
import random
from datetime import datetime, date
import numpy as np
"""
Radomly selects dinners based on the additional metadata provided. Currently, the script only supports weighting the meals with the season.
Eventually there may be some other useful things to weight meals by and or categorize them. 

Potential new metadata:
- Difficulty
  - How much of a pain in the ass is it to make? I dont want 5 meals that take 4 hours and every dish in the house to make
- Leftover output
  - I want 1 or 2 meals every week that give me some good leftovers for lunches the rest of the week.


 The dinner list file example structure and is currently in `~/.config/dinners/list.json`:
 ```
 [
   {
       "name": "Pizza",
       "recipe" : "url", // optional
   },
   ...
 ]

```
"""

def main():

    with open("/home/riley/.config/dinners/list.json", 'r', encoding='utf-8') as dinner_file:
        dinner = json.load(dinner_file)


        season = "any"
        current_month = date.today().month
        if current_month in [1,2,10,11,12]:
            season = "winter"
        elif current_month in [6,7,8]:
            season = "summer"



        total_weight = 0
        meal_names = []
        probabilities = []
        for meal in dinner:
            weight = 1
            if meal["season"] == "winter" and season == "summer":
                weight = .25
            elif meal["season"] == "summer" and season == "winter":
                weight = .5 #I'm more likely to grill in the winter than have soup in the summer

            meal_names.append(meal["name"])
            probabilities.append(weight)
            total_weight += weight
        

        probabilities = [p / total_weight for p in probabilities]

        this_week = np.random.choice(meal_names, 5, replace=False, p=probabilities)
        for dinner in this_week:
            print(dinner)


if __name__ == "__main__":
    main()
