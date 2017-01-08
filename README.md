# Personal Expense Tracker

Spring based web app for personal accounting.

![Screenshot should be here](pet_screenshot.jpg)

While it is a pretty simple CRUD, I did get some hands-on experience with:
- Secure authentication for users (hashed and salted passwords, brute force detection, etc.)
- End-to-end encryption with SSL certificates with automated renewal (LetsEncrypt for public-facing certificate and my own CA for traffic between 2 AWS instances)
- Databases (schema design, migrations, etc.)