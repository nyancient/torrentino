# torrentino
Anime and TV series RSS scraper + torrent downloader


## Usage
`docker pull` and start `ghcr.io/foxolotl/torrentino:main`.
Use [docker-compose.yml](docker-compose.yml) as a base for your own deployment if you like.

To add series to your watch list, SSH into your container, as user `torrentino` and use the torrentino shell.
By default, [Nyaa Torrents](https://nyaa.si) is checked for updates to your watched series every hour,
but you can also use the shell to manually download new episodes.

When a new episode is found, its corresponding torrent file is downloaded and sent to the included
[Transmission](https://transmissionbt.com/) BitTorrent client, which proceeds to download it into whichever directory
you mounted as the `/downloads` volume.
You can manage your ongoing downloads by navigating to `http://the-torrentino-container` using your browser and
authenticating yourself using the credentials given by the `WEB_USER` and `WEB_PASSWORD` environment variables.


## Configuration
To configure additional RSS sources or episode check interval, modify [config.toml](config.toml) and mount it
over `/config.toml`.

To change which user owns downloaded files, rebuild the image with build arg `UID` set to the numeric user ID
of the appropriate user.

### Volumes
- `/downloads` - downloaded episodes are placed here
- `/data` - internal torrentino data that needs to persist across reboots, such as episode watch lists
- `/authorized_keys` - public key(s) allowed to access the torrentino shell
- `/config.toml` - torrentino configuration file

### Environment
- `BT_PORT` - port on which to listen for incoming BitTorrent connections
- `WEB_USER` - username for authenticating with the Transmission web client
- `WEB_PASSWORD` - password for authenticating with the Transmission web client (stored in plaintext; **don't reuse!**)


## Troubleshooting

### I can't log in over SSH
Make sure you've mounted your public key (or `authorized_keys` file) as the `/authorized_keys` volume.

### How can I run the web interface over HTTPS?
Make sure you've mounted your public key (or `authorized_keys` file) as the `/authorized_keys` volume.

### Downloaded episodes are owned by a weird user
Rebuild torrentino with build arg `UID` set to the numeric user ID of the user that should own the downloaded
files.
