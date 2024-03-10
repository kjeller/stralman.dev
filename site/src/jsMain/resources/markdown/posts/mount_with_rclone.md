---
root: .components.layouts.MarkdownLayout
title: "Mount your cloud storage with rclone"
date: 2023-01-07
author: Karl Strålman
tags:
  - "tools"
---

So I recently came across an awesome tool called rclone. rclone is a command-line tool with many
features for managing cloud storage - even so that people refer to it
as [the swiss army knife of cloud storage](https://rclone.org/).

This post will focus on how to configure remotes and how to mount them.
I wrote this mostly as a future reference for myself but also to show a fraction of rclone's
capabilities and why others might find it useful.

Configuring a rclone remote is straightforward - basically, just use `rclone config` and follow its
interactive setup guide. Enabling it to mount automatically was a bit more involved but not too
advanced.

- [Configure a rclone remote - dropbox remote example](#configure-a-rclone-remote)
- [Configure remote to mount on boot](#mount-a-remote-at-every-boot)

## Configure a rclone remote

**Prerequisite:** [install rclone](https://rclone.org/install/)

My dropbox remote configuration prompts differed slightly from what was stated
in [the documentation](https://rclone.org/dropbox/) which is mainly the reason why I felt like I had
to include this section:

```bash
$ rclone config
No remotes found - make a new one
n) New remote
s) Set configuration password
q) Quit config

# 'n' for new remote
n/s/q>n

# What I chose to name my remote
name>dropbox-remote
Type of storage to configure.                          
Enter a string value. Press Enter for the default ("").
Choose a number from below, or type in your own value  
...
 9 / Dropbox    
   \ "dropbox"  
...

# Select '9' for Dropbox remote backend
Storage>9
** See help for dropbox backend at: https://rclone.org/dropbox/ **

Dropbox App Client Id
Leave blank normally.
Enter a string value. Press Enter for the default ("").

# Here I entered blank
client_id>
Dropbox App Client Secret
Leave blank normally.
Enter a string value. Press Enter for the default ("").

# Here I entered blank
client_secret>
Edit advanced config? (y/n)
y) Yes
n) No

# 'n' for no
y/n>n
Remote config
Use auto config?
 * Say Y if not sure
 * Say N if you are working on a remote or headless machine
y) Yes
n) No

# 'y' for auto config
y/n>y
# And then I had to authenticate using a dropbox token - and that was it!
```

Now I was able to list my dropbox directions using `rclone lsd dropbox-remote:`

## Mount a remote at every boot

**Prerequisite:** [install rclone](https://rclone.org/install/) & configure a remote
e.g. [dropbox](#configure-a-rclone-remote)

One alternative to mounting a remote automatically is by using a templated user systemd
service, [found here](https://github.com/rclone/rclone/wiki/Systemd-rclone-mount#systemd). The last
edit date on this wiki page is October 6 2022 at the time of writing make sure you are using the
latest version.

1. Create a service file

- Save the following content to this file: `/etc/systemd/user/rclone@.service`

```service
[Unit]
Description=RClone mount of users remote %i using filesystem permissions
Documentation=http://rclone.org/docs/
After=network-online.target


[Service]
Type=notify
#Set up environment
Environment=REMOTE_NAME="%i"
Environment=REMOTE_PATH="/"
Environment=MOUNT_DIR="%h/%i"
Environment=POST_MOUNT_SCRIPT=""
Environment=RCLONE_CONF="%h/.config/rclone/rclone.conf"
Environment=RCLONE_TEMP_DIR="/tmp/rclone/%u/%i"
Environment=RCLONE_RC_ON="false"

#Default arguments for rclone mount. Can be overridden in the environment file
Environment=RCLONE_MOUNT_ATTR_TIMEOUT="1s"
#TODO: figure out default for the following parameter
Environment=RCLONE_MOUNT_DAEMON_TIMEOUT="UNKNOWN_DEFAULT"
Environment=RCLONE_MOUNT_DIR_CACHE_TIME="60m"
Environment=RCLONE_MOUNT_DIR_PERMS="0777"
Environment=RCLONE_MOUNT_FILE_PERMS="0666"
Environment=RCLONE_MOUNT_GID="%G"
Environment=RCLONE_MOUNT_MAX_READ_AHEAD="128k"
Environment=RCLONE_MOUNT_POLL_INTERVAL="1m0s"
Environment=RCLONE_MOUNT_UID="%U"
Environment=RCLONE_MOUNT_UMASK="022"
Environment=RCLONE_MOUNT_VFS_CACHE_MAX_AGE="1h0m0s"
Environment=RCLONE_MOUNT_VFS_CACHE_MAX_SIZE="off"
Environment=RCLONE_MOUNT_VFS_CACHE_MODE="off"
Environment=RCLONE_MOUNT_VFS_CACHE_POLL_INTERVAL="1m0s"
Environment=RCLONE_MOUNT_VFS_READ_CHUNK_SIZE="128M"
Environment=RCLONE_MOUNT_VFS_READ_CHUNK_SIZE_LIMIT="off"
#TODO: figure out default for the following parameter
Environment=RCLONE_MOUNT_VOLNAME="UNKNOWN_DEFAULT"

#Overwrite default environment settings with settings from the file if present
EnvironmentFile=-%h/.config/rclone/%i.env

#Check that rclone is installed
ExecStartPre=/usr/bin/test -x /usr/bin/rclone

#Check the mount directory
ExecStartPre=/usr/bin/test -d "${MOUNT_DIR}"
ExecStartPre=/usr/bin/test -w "${MOUNT_DIR}"
#TODO: Add test for MOUNT_DIR being empty -> ExecStartPre=/usr/bin/test -z "$(ls -A "${MOUNT_DIR}")"

#Check the rclone configuration file
ExecStartPre=/usr/bin/test -f "${RCLONE_CONF}"
ExecStartPre=/usr/bin/test -r "${RCLONE_CONF}"
#TODO: add test that the remote is configured for the rclone configuration

#Mount rclone fs
ExecStart=/usr/bin/rclone mount \
            --config="${RCLONE_CONF}" \
#See additional items for access control below for information about the following 2 flags
#            --allow-other \
#            --default-permissions \
            --rc="${RCLONE_RC_ON}" \
            --cache-tmp-upload-path="${RCLONE_TEMP_DIR}/upload" \
            --cache-chunk-path="${RCLONE_TEMP_DIR}/chunks" \
            --cache-workers=8 \
            --cache-writes \
            --cache-dir="${RCLONE_TEMP_DIR}/vfs" \
            --cache-db-path="${RCLONE_TEMP_DIR}/db" \
            --no-modtime \
            --drive-use-trash \
            --stats=0 \
            --checkers=16 \
            --bwlimit=40M \
            --cache-info-age=60m \
            --attr-timeout="${RCLONE_MOUNT_ATTR_TIMEOUT}" \
#TODO: Include this once a proper default value is determined
#           --daemon-timeout="${RCLONE_MOUNT_DAEMON_TIMEOUT}" \
            --dir-cache-time="${RCLONE_MOUNT_DIR_CACHE_TIME}" \
            --dir-perms="${RCLONE_MOUNT_DIR_PERMS}" \
            --file-perms="${RCLONE_MOUNT_FILE_PERMS}" \
            --gid="${RCLONE_MOUNT_GID}" \
            --max-read-ahead="${RCLONE_MOUNT_MAX_READ_AHEAD}" \
            --poll-interval="${RCLONE_MOUNT_POLL_INTERVAL}" \
            --uid="${RCLONE_MOUNT_UID}" \
            --umask="${RCLONE_MOUNT_UMASK}" \
            --vfs-cache-max-age="${RCLONE_MOUNT_VFS_CACHE_MAX_AGE}" \
            --vfs-cache-max-size="${RCLONE_MOUNT_VFS_CACHE_MAX_SIZE}" \
            --vfs-cache-mode="${RCLONE_MOUNT_VFS_CACHE_MODE}" \
            --vfs-cache-poll-interval="${RCLONE_MOUNT_VFS_CACHE_POLL_INTERVAL}" \
            --vfs-read-chunk-size="${RCLONE_MOUNT_VFS_READ_CHUNK_SIZE}" \
            --vfs-read-chunk-size-limit="${RCLONE_MOUNT_VFS_READ_CHUNK_SIZE_LIMIT}" \
#TODO: Include this once a proper default value is determined
#            --volname="${RCLONE_MOUNT_VOLNAME}"
            "${REMOTE_NAME}:${REMOTE_PATH}" "${MOUNT_DIR}"

#Execute Post Mount Script if specified
ExecStartPost=/bin/sh -c "${POST_MOUNT_SCRIPT}"

#Unmount rclone fs
ExecStop=/bin/fusermount -u "${MOUNT_DIR}"

#Restart info
Restart=always
RestartSec=10

[Install]
WantedBy=default.target

```

2. Tell systemd to look for new files.
   `systemctl --user daemon-reload`

3. (Optional step) configure a custom mount point. The default mount directory is `~/<REMOTE NAME>`
   if you are ok with that skip this step.

- Create an env file: `touch ~/.config/rclone/<REMOTE NAME>.env`. For me this file
  is `~/.config/rclone/dropbox-remote.env`
- Set the env variable *MOUNT_DIR* to whatever mounting directory you want. This directory must
  exist in the filesystem. For example I want dropbox-remote to moount in /mnt so I
  set `MOUNT_DIR=/mnt/dropbox-remote`. Then create that folder
  e.g. `sudo mkdir /mnt/dropbox-remote && sudo chown $USER /mnt/dropbox-remote`

4. Start the templated systemd user service for a specific remote.

`systemctl --user enable rclone@<REMOTE NAME>`
`systemctl --user start rclone@<REMOTE NAME>`

e.g. for my dropbox this was:
`systemctl --user enable rclone@dropbox-remote`
`systemctl --user start rclone@dropbox-remote`

Now your remote should mount for every reboot.

## Checking out VFS cache modes

*Edit: Jan 8, 2022*

The default VFS cache mode `Environment=RCLONE_MOUNT_VFS_CACHE_MODE="off"` or `--vfs-cache-mode off`
which it evaluates to prevent my password manager from opening my database file from the remote
mount directory for both read and write. I get the following error log from doing
this: `WriteFileHandle: Can't open for write without O_TRUNC`.

Changing the cache mode from `--vfs-cache-mode off` to `--vfs-cache-mode writes` should support all
normal file operations (Source [here](https://rclone.org/commands/rclone_mount/#file-caching)).

2. Restart service: `systemctl --user restart rclone@<REMOTE NAME>`
3. Check that it was restarted correctly with correct cache
   mode: `systemctl --user status rclone@<REMOTE NAME>`
