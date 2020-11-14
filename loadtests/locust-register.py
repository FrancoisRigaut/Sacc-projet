import random
import time
from locust import HttpUser, task, between


class QuickstartUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        user = {
            "phone": "0"+str(time.time())+str(random.randint(1, 180)),
        }
        self.client.post("/ws/user/register", json=user,
                         headers={"Content-Type": "application/json", "Origin": '*'})
