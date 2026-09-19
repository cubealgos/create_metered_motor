---
schema_version: 1
id: 01M2W337Y89QJVWYAK2QHX44JC
key: M2
title: "The trade: roll, tags, one motor per villager"
status: todo
created_at: 2026-09-19T05:44:15Z
---

## Goal

A motor enters the world through the toolsmith: three tiers of trade, each rolled once when the offer is created, and a villager never holding more than one motor offer at a time.

## Scope

In: the three trade files and their tag entries, the `metered_motor:roll` loot function and its band parameters, the `metered_motor:no_motor_offered` merchant predicate, the offer's tooltip (MM-5). Out: the motor's own behaviour once bought (M1, already built) and how the item is read afterwards beyond the trade-screen tooltip (M3).

## Exit criteria

- The trade files parse and the vanilla toolsmith tags contain them.
- A mock toolsmith offer carries a roll inside its tier's band.
- A villager already offering a motor does not draw a second.
- The open question — whether `merchant_predicate` sees the villager's offers — is resolved and recorded, one way or the other.

## Tickets

- MM-5 — Trades: three tiers, tag entries, the roll loot function, one motor per villager

## Depends on

M1: MM-5 is blocked by MM-2, the stats component and roll arithmetic the loot function writes.
