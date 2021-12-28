#!/bin/sh
mkdir -p /data/transmission-daemon
chown -R $UID:nogroup /torrents /data/transmission-daemon
chown $UID:nogroup /downloads /data /authorized_keys

if [ ! -f "/data/ssh_host_rsa_key" ]; then
	ssh-keygen -f /data/ssh_host_rsa_key -N '' -t rsa
fi
if [ ! -f "/data/ssh_host_ecdsa_key" ]; then
	ssh-keygen -f /data/ssh_host_ecdsa_key -N '' -t ecdsa
fi
if [ ! -f "/data/ssh_host_ed25519_key" ]; then
	ssh-keygen -f /data/ssh_host_ed25519_key -N '' -t ed25519
fi

if [ ! -d "/var/run/sshd" ]; then
  mkdir -p /var/run/sshd
fi

echo Starting sshd...
/usr/sbin/sshd -D &

echo Starting transmission-daemon...
setpriv --reuid=$UID \
        --regid=nogroup \
        --init-groups \
        --inh-caps=-all \
        transmission-daemon -P 6881 -p 8080 -a *.*.*.* -c /torrents -w /downloads -t -u $WEB_USER -v $WEB_PASSWORD -g /data/transmission-daemon

echo Starting torrentino....
exec setpriv --reuid=$UID \
        --regid=nogroup \
        --init-groups \
        --inh-caps=-all \
        $JAVA_HOME/bin/java -jar /torrentino.jar -c /config.toml -w
