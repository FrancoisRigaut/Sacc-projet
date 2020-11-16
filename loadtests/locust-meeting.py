import random
import time
from locust import HttpUser, task, between


class QuickstartUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        meeting = {
            "sha1": "0120708f9efd14236f4f7d0901dbea93b334228c",
            "sha1Met": "01465044bb81dce8044fd2d9c063f99cd1a36325",
            "gps": {
                "latitude": random.uniform(1.0, 50.0),
                "longitude": random.uniform(1.0, 50.0),
            },
            "timestamp": str(time.time()),
        }
        self.client.post("/ws/meeting/register", json=meeting,
                         headers={"Content-Type": "application/json", "Origin": '*'})
