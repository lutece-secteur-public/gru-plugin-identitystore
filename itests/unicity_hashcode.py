#!/usr/bin/env python3
"""
Script de test d'identités via l'API IdentityStore. Généré pas notre ami Claude.

- test_create       : deux créations consécutives avec la même identité (doublon)
- test_update       : crée A et B, puis update B pour qu'elle ressemble à A (doublon)
- test_update_ok    : crée A, puis update family_name et first_name avec des valeurs différentes (non régression)
- test_merge        : crée A et B, puis merge B (secondary) dans A (primary) avec le champ identity
- test_cancel_merge : crée A et B, merge B dans A (sans identity), puis annule le merge (unmerge)
"""

import requests
import random
import string
import time
import json
import sys
import unicodedata

# --- Configuration ---
URL = "http://localhost:8080/identity/rest/identitystore/v3/identity"
MERGE_URL = f"{URL}/merge"
UNMERGE_URL = f"{URL}/unmerge"
HEADERS = {
    "client_code": "TEST",
    "application_code": "TEST",
    "author_name": "test",
    "author_type": "owner",
    "Content-Type": "application/json",
}
NB_CALLS = 2

# --- Listes pour la génération aléatoire ---

FIRST_NAMES = [
    "Alice", "Bruno", "Camille", "David", "Emma", "Fabien", "Gabrielle",
    "Hugo", "Inès", "Julien", "Karine", "Léo", "Marie", "Nathan",
    "Olivia", "Paul", "Raphaël", "Sophie", "Thomas", "Valérie",
]

FAMILY_NAMES = [
    "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit",
    "Durand", "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel",
    "Garcia", "David", "Bertrand", "Roux", "Vincent", "Fournier",
]


def random_string(length: int) -> str:
    return "".join(random.choices(string.ascii_lowercase, k=length))


def strip_accents(text: str) -> str:
    """Supprime les accents d'une chaîne (é -> e, è -> e, etc.)."""
    return "".join(
        c for c in unicodedata.normalize("NFD", text)
        if unicodedata.category(c) != "Mn"
    )


def attr(key: str, value: str, cert_date: int) -> dict:
    return {
        "key": key,
        "value": value,
        "type": "string",
        "certificationLevel": 100,
        "certProcess": "DEC",
        "certDate": cert_date,
    }


def generate_base_identity() -> dict:
    """Génère les données fixes partagées par tous les appels."""
    first_name = random.choice(FIRST_NAMES)
    family_name = random.choice(FAMILY_NAMES)
    email = f"{strip_accents(first_name).lower()}.{strip_accents(family_name).lower()}.{random.randint(1000, 9999)}@example.com"
    return {
        "first_name": first_name,
        "family_name": family_name,
        "email": email,
        "preferred_username": random_string(8),
        "phone": f"06{random.randint(10000000, 99999999)}",
        "birthdate": f"{random.randint(1, 28):02d}/{random.randint(1, 12):02d}/{random.randint(1960, 2005)}",
        "cert_date": int(time.time() * 1000),
    }


def build_payload(base: dict) -> dict:
    """Construit le payload à partir des données de base."""
    cd = base["cert_date"]

    return {
        "identity": {
            "attributes": [
                attr("email", base["email"], cd),
                attr("login", base["email"], cd),
                attr("birthdate", base["birthdate"], cd),
                attr("preferred_username", base["preferred_username"], cd),
                attr("birthplace", "FAYET", cd),
                attr("birthplace_code", "02303", cd),
                attr("mobile_phone", base["phone"], cd),
                attr("birthcountry", "FRANCE", cd),
                attr("birthcountry_code", "99100", cd),
                attr("family_name", base["family_name"], cd),
                attr("first_name", base["first_name"], cd),
                attr("gender", "2", cd),
            ]
        }
    }


def create_identity(base: dict) -> str | None:
    """Crée une identité et retourne le customer_id, ou None en cas d'erreur."""
    payload = build_payload(base)
    print(f"    email/login : {base['email']}")
    try:
        resp = requests.post(URL, headers=HEADERS, json=payload)
        print(f"    Status : {resp.status_code}")
        try:
            body = resp.json()
            print(json.dumps(body, indent=2, ensure_ascii=False))
            return body.get("customer_id")
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)
    return None


def get_identity(customer_id: str) -> dict | None:
    """Récupère une identité par son customer_id. Retourne le body JSON ou None."""
    get_url = f"{URL}/{customer_id}"
    print(f"    GET {get_url}")
    try:
        resp = requests.get(get_url, headers=HEADERS)
        print(f"    Status : {resp.status_code}")
        try:
            body = resp.json()
            print(json.dumps(body, indent=2, ensure_ascii=False))
            return body
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)
    return None


def update_identity(customer_id: str, base: dict) -> None:
    """GET l'identité pour récupérer last_update_date, puis PUT avec les nouvelles données."""
    # GET pour récupérer last_update_date
    print("  [GET avant update]")
    current = get_identity(customer_id)
    if not current:
        print("ERREUR : impossible de récupérer l'identité, abandon de l'update.", file=sys.stderr)
        return

    last_update_date = None
    identities = current.get("identities", [])
    if identities:
        last_update_date = identities[0].get("last_update_date")

    if last_update_date is None:
        print("ERREUR : last_update_date absent de la réponse GET, abandon.", file=sys.stderr)
        return

    print(f"    last_update_date = {last_update_date}")

    # PUT avec last_update_date injecté
    payload = build_payload(base)
    payload["identity"]["last_update_date"] = last_update_date

    update_url = f"{URL}/{customer_id}"
    print(f"\n  [PUT update]")
    print(f"    PUT {update_url}")
    print(f"    email/login : {base['email']}")
    try:
        resp = requests.put(update_url, headers=HEADERS, json=payload)
        print(f"    Status : {resp.status_code}")
        try:
            print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)


# ---------------------------------------------------------------------------


def test_create():
    """Teste deux créations consécutives avec la même identité."""
    print("=" * 60)
    print("TEST CREATE : deux créations identiques consécutives")
    print("=" * 60)

    base = generate_base_identity()
    print("\nDonnées de base :")
    print(json.dumps(base, indent=2, ensure_ascii=False))

    for i in range(1, NB_CALLS + 1):
        print(f"\n--- Create {i}/{NB_CALLS} ---")
        create_identity(base)

    print()


def test_update():
    """Crée deux identités différentes puis update la seconde pour qu'elle ressemble à la première."""
    print("=" * 60)
    print("TEST UPDATE : create A, create B, puis update B -> A")
    print("=" * 60)

    identity_a = generate_base_identity()
    identity_b = generate_base_identity()

    print("\nIdentité A :")
    print(json.dumps(identity_a, indent=2, ensure_ascii=False))
    print("\nIdentité B :")
    print(json.dumps(identity_b, indent=2, ensure_ascii=False))

    # Créer A
    print("\n--- Create A ---")
    customer_id_a = create_identity(identity_a)
    if not customer_id_a:
        print("ERREUR : impossible de créer l'identité A, abandon.", file=sys.stderr)
        return

    # Créer B
    print("\n--- Create B ---")
    customer_id_b = create_identity(identity_b)
    if not customer_id_b:
        print("ERREUR : impossible de créer l'identité B, abandon.", file=sys.stderr)
        return

    print(f"\n    customer_id A = {customer_id_a}")
    print(f"    customer_id B = {customer_id_b}")

    # Update B avec les données de A
    print("\n--- Update B -> A ---")
    update_identity(customer_id_b, identity_a)

    print()


def get_last_update_date(customer_id: str) -> int | None:
    """GET une identité et retourne son last_update_date, ou None."""
    body = get_identity(customer_id)
    if not body:
        return None
    identities = body.get("identities", [])
    if identities:
        return identities[0].get("last_update_date")
    return None


def test_merge():
    """Crée deux identités A et B, puis merge B (secondary) dans A (primary)."""
    print("=" * 60)
    print("TEST MERGE : create A, create B, puis merge B dans A")
    print("=" * 60)

    identity_a = generate_base_identity()
    identity_b = generate_base_identity()

    print("\nIdentité A (primary) :")
    print(json.dumps(identity_a, indent=2, ensure_ascii=False))
    print("\nIdentité B (secondary) :")
    print(json.dumps(identity_b, indent=2, ensure_ascii=False))

    # Créer A
    print("\n--- Create A ---")
    customer_id_a = create_identity(identity_a)
    if not customer_id_a:
        print("ERREUR : impossible de créer l'identité A, abandon.", file=sys.stderr)
        return

    # Créer B
    print("\n--- Create B ---")
    customer_id_b = create_identity(identity_b)
    if not customer_id_b:
        print("ERREUR : impossible de créer l'identité B, abandon.", file=sys.stderr)
        return

    # GET A et B pour récupérer les last_update_date
    print("\n--- GET A ---")
    lud_a = get_last_update_date(customer_id_a)
    if lud_a is None:
        print("ERREUR : last_update_date absent pour A, abandon.", file=sys.stderr)
        return

    print("\n--- GET B ---")
    lud_b = get_last_update_date(customer_id_b)
    if lud_b is None:
        print("ERREUR : last_update_date absent pour B, abandon.", file=sys.stderr)
        return

    print(f"\n    customer_id A = {customer_id_a}  (last_update_date = {lud_a})")
    print(f"    customer_id B = {customer_id_b}  (last_update_date = {lud_b})")

    # Merge B dans A
    identity_b_payload = build_payload(identity_b)
    merge_payload = {
        "primary_customer_id": customer_id_a,
        "primary_last_update_date": lud_a,
        "secondary_customer_id": customer_id_b,
        "secondary_last_update_date": lud_b,
        "duplicate_rule_code": "RG_GEN_SuspectDoublon_09",
        "identity": identity_b_payload["identity"],
    }

    print("\n--- Merge B -> A ---")
    print(f"    POST {MERGE_URL}")
    print(json.dumps(merge_payload, indent=2))
    try:
        resp = requests.post(MERGE_URL, headers=HEADERS, json=merge_payload)
        print(f"    Status : {resp.status_code}")
        try:
            print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)

    print()


def test_cancel_merge():
    """Crée A et B, merge B dans A, puis annule le merge (unmerge)."""
    print("=" * 60)
    print("TEST CANCEL MERGE : create A, create B, merge, puis unmerge")
    print("=" * 60)

    identity_a = generate_base_identity()
    identity_b = generate_base_identity()

    print("\nIdentité A (primary) :")
    print(json.dumps(identity_a, indent=2, ensure_ascii=False))
    print("\nIdentité B (secondary) :")
    print(json.dumps(identity_b, indent=2, ensure_ascii=False))

    # Créer A
    print("\n--- Create A ---")
    customer_id_a = create_identity(identity_a)
    if not customer_id_a:
        print("ERREUR : impossible de créer l'identité A, abandon.", file=sys.stderr)
        return

    # Créer B
    print("\n--- Create B ---")
    customer_id_b = create_identity(identity_b)
    if not customer_id_b:
        print("ERREUR : impossible de créer l'identité B, abandon.", file=sys.stderr)
        return

    # GET A et B pour last_update_date avant merge
    print("\n--- GET A ---")
    lud_a = get_last_update_date(customer_id_a)
    if lud_a is None:
        print("ERREUR : last_update_date absent pour A, abandon.", file=sys.stderr)
        return

    print("\n--- GET B ---")
    lud_b = get_last_update_date(customer_id_b)
    if lud_b is None:
        print("ERREUR : last_update_date absent pour B, abandon.", file=sys.stderr)
        return

    print(f"\n    customer_id A = {customer_id_a}  (last_update_date = {lud_a})")
    print(f"    customer_id B = {customer_id_b}  (last_update_date = {lud_b})")

    # Merge B dans A (sans champ identity pour ce scénario)
    merge_payload = {
        "primary_customer_id": customer_id_a,
        "primary_last_update_date": lud_a,
        "secondary_customer_id": customer_id_b,
        "secondary_last_update_date": lud_b,
        "duplicate_rule_code": "RG_GEN_SuspectDoublon_09",
    }

    print("\n--- Merge B -> A ---")
    print(f"    POST {MERGE_URL}")
    try:
        resp = requests.post(MERGE_URL, headers=HEADERS, json=merge_payload)
        print(f"    Status : {resp.status_code}")
        try:
            print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)

    # GET A après merge pour récupérer le nouveau last_update_date
    print("\n--- GET A (après merge) ---")
    lud_a_after = get_last_update_date(customer_id_a)
    if lud_a_after is None:
        print("ERREUR : last_update_date absent pour A après merge, abandon.", file=sys.stderr)
        return

    # GET B après merge pour récupérer le nouveau last_update_date
    print("\n--- GET B (après merge) ---")
    lud_b_after = get_last_update_date(customer_id_b)
    if lud_b_after is None:
        print("ERREUR : last_update_date absent pour B après merge, abandon.", file=sys.stderr)
        return

    # Cancel merge (unmerge)
    unmerge_payload = {
        "primary_customer_id": customer_id_a,
        "primary_last_update_date": lud_a_after,
        "secondary_customer_id": customer_id_b,
        "secondary_last_update_date": lud_b_after,
        "duplicate_rule_code": "RG_GEN_SuspectDoublon_09",
    }

    print("\n--- Cancel Merge (Unmerge) ---")
    print(f"    POST {UNMERGE_URL}")
    print(json.dumps(unmerge_payload, indent=2))
    try:
        resp = requests.post(UNMERGE_URL, headers=HEADERS, json=unmerge_payload)
        print(f"    Status : {resp.status_code}")
        try:
            print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
        except ValueError:
            print(resp.text)
    except requests.RequestException as e:
        print(f"    Erreur : {e}", file=sys.stderr)

    print()


def test_update_ok():
    """Crée une identité A, puis met à jour family_name et first_name avec des valeurs différentes."""
    print("=" * 60)
    print("TEST UPDATE OK : create A, puis update family_name + first_name")
    print("=" * 60)

    identity_a = generate_base_identity()

    print("\nIdentité A (initiale) :")
    print(json.dumps(identity_a, indent=2, ensure_ascii=False))

    # Créer A
    print("\n--- Create A ---")
    customer_id_a = create_identity(identity_a)
    if not customer_id_a:
        print("ERREUR : impossible de créer l'identité A, abandon.", file=sys.stderr)
        return

    # Générer des noms différents de ceux de A
    new_first = random.choice([n for n in FIRST_NAMES if n != identity_a["first_name"]])
    new_family = random.choice([n for n in FAMILY_NAMES if n != identity_a["family_name"]])

    # Copier l'identité et modifier les noms
    identity_a_updated = dict(identity_a)
    identity_a_updated["first_name"] = new_first
    identity_a_updated["family_name"] = new_family

    print(f"\n    Changement : {identity_a['first_name']} {identity_a['family_name']}"
          f" -> {new_first} {new_family}")

    # Update
    print("\n--- Update A ---")
    update_identity(customer_id_a, identity_a_updated)

    print()


# ---------------------------------------------------------------------------


def main():
    test_create()
    test_update()
    test_update_ok()
    test_merge()
    test_cancel_merge()


if __name__ == "__main__":
    main()