import random


def toss_coin(prob_head):
    # Simulate a coin toss based on the probability of getting Heads
    return "H" if random.random() < prob_head else "T"


def play_round(num_players):
    scores = [0] * num_players  # Initialize scores for each player in the current round
    for player in range(num_players):
        print(f"Player {player + 1}:")
        coins = []  # Hold the toss results for this player's three coins
        for coin_num in range(1, 4):
            prob_head = random.randint(0, 100) / 100  # Get a random probability for Heads
            print(f"Coin {coin_num} probability for Heads: {prob_head:.0%}")
            toss = toss_coin(prob_head)  # Toss the coin based on the probability for Heads
            print(f"Coin {coin_num} toss result: {toss}")
            coins.append(toss)  # Store the toss result in the coins list
        if len(set(coins)) == 1:
            scores[player] += 1  # If all three coins show the same side, add a point
        print()
    return scores  # Return the scores for this round


def play_game(num_players, num_rounds):
    total_scores = [0] * num_players  # Initialize total scores for each player

    for round_num in range(1, num_rounds + 1):
        print(f"Round {round_num}:")
        print()
        round_scores = play_round(num_players)  # Play the round and get the scores
        for player, score in enumerate(round_scores):
            total_scores[player] += score  # Update the player's total scores
            print(f"Player {player + 1} wins {score} point(s) this round.")
        print()

    # Display the final results after all rounds
    print("Final Results:")
    for player, score in enumerate(total_scores):
        print(f"Player {player + 1} scored {score} point(s).")


if __name__ == "__main__":
    num_players = 3
    num_rounds = 5
    play_game(num_players, num_rounds)  # Start the Triplet Toss game with the given parameters
