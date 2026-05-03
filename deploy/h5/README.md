# Dokploy H5 deployment

Deploys `L2J_Mobius_CT_2.6_HighFive` as a private High Five test stack.

## Compose path

```txt
deploy/h5/docker-compose.yml
```

## Services

- `mariadb` — internal database, persisted in `l2h5-mariadb-data`.
- `login` — public login port `2106`, internal game-link port `9014`.
- `game` — public game port `7777`.

## Required Dokploy environment

Copy values from `deploy/h5/.env.example` and replace:

- `DB_PASSWORD`
- `DB_ROOT_PASSWORD`

## Client

Use a Lineage II High Five client. The server allows protocol revisions:

```txt
267;268;271;273
```

Patch/edit the H5 client's `system/l2.ini` so `ServerAddr` points to:

```txt
172.238.199.84
```
