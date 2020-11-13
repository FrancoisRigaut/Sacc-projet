import random
import time
from locust import HttpUser, task, between


class QuickstartUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        meeting = {
            "sha1": "usr-"str(time.time()),
            "sha1Met": "usrMet-"+str(time.time()),
            "gps": {
                "latitude": random.uniform(1.0, 50.0),
                "longitude": random.uniform(1.0, 50.0),
            },
            "timestamp": str(time.time()),
        }
        self.client.post("/ws/meeting/register", json=meeting,
                         headers={"Content-Type": "application/json", "Origin": '*'})
