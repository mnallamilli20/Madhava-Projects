# Madhava Nallamilli     10/4/2023
# Assingnment 3
# The following code is a program that allows the user to play a game agaisnt another human or and AI. When they start the code, they are given 3 options. 1 option to play, 1 options for the instructions for the game, and 1 option to quit the game.

import random


def printTitleMaterial():
    """ This function prints the 'title material' that prints out when the program starts.
    """
    print("Spinner Winner!")
    print()

   
    print("By: Madhava Nallamilli")
    print("[COM S 127 Section G]")
    print()

def initialChoice():
    """ This function allows the player to make various choices when starting the game. This is an
    example of the 'do-while' pattern.

    :return String: The choice the player has made when starting the program to [p]lay the game, view the
    [i]nstructions, or [q]uit the program..
    """
    choice = input("Choice? [p]lay, [i]nstructions, [q]uit: ")
    while choice != "p" and choice != "i" and choice != "q":
        print("ERROR: Please enter 'p', 'i', or 'q'...")
        choice = input("Choice? [p]lay, [i]nstructions, [q]uit: ")
    return choice

def chooseNumPlayers():
    """ This function allows the player to choose whether they will play against another
    human, or play against the computer.

    :return Integer: The number of players in the game.
    """
    numPlayers = 0
   
   
    while True:
        user = int(input("Are you playing against a human or computer?\n1: Computer\n2: Human\n"))
        if user == 1:
            numPlayers+=1
            break
           
        elif user == 2:
            numPlayers+=2
            break
        else:
            print("Please enter 1 or 2.")
           
    # NOTE: The only valid inputs should be '1' and '2'
    #       If the user chooses '1,' then they play against the computer
    #       If the user chooses '2,' then they play against another human
    return numPlayers

def wait():
    """ This function has the computer 'wait' until the [Enter] key is pressed. This allows
    for better 'readability' in the final output.
    """
    input("Press [Enter] To Continue...")
    print()

def printBanner():
    """ Prints the 'banner' between each round of the game so that the output text does not
    get too 'messy.'
    """
    print("#######################################################################")
    print()
    print("~~ Starting New Round ~~")
    print()

def printPoints(playerNum, points):
    """ This function prints the number of points a certain player currently has.

    :param Integer playerNum: The player whose points are being displayed.
    :param Integer points: The number of points to be displayed
    """
    print("* Player {0} Has {1} Points!".format(playerNum, points))
    print()

def wagerPointsHuman(playerNum, points):
    """ This function uses the 'do-while' pattern to take in input for the number of points to be wagered. It checks to
    make sure that the player has entered a valid amount of points. Meaning, the player cannot wager more points than
    they have, nor zero points, nor a negative number of points. The function then returns the wager.

    As there could potentially be two human players, this function requires the playerNum to know which player to include
    in any printouts.

    :param Integer playerNum: The player to include in any printouts. Can be 1 or 2.
    :param Integer points: The number of points the specified player currently has.
    :return Integer: The number of points to be wagered.
    """
   
    while True:
        wager = int(input("Player {0}, enter the number of points to wager: ".format(playerNum)))
        if 0 < wager <= points:
            break
        else:
            print("Please enter a positive number less than or equal to the amount of points you have.")
   
    print("* Player {0} Has Wagered {1} Points!".format(playerNum, wager))
    return wager

def wagerPointsAI(playerNum, points):
    """ This function should choose a random number between 1 and the number of points the AI has (inclusive). For example,
    if the computer has 5 points, it can wager either 1, 2, 3, 4, or 5. This function should include a printout similar to
    what is printed when the human wagers points.

    :param Integer playerNum: The player to include in any printouts. While this will usually be 2, including this value allows
                              for a future version of the game with 2 computer players.
    :param Integer points: The number of points the specified player currently has.
    :return Integer: The number of points to be wagered.
    """
    comWager = 0
   
    comWager = random.randrange(1, (points+1))
    # NOTE: Try to have the AI version match the look of the output of the human version above
    print("* Player {0} Has Wagered {1} Points!".format(playerNum, points))
    return comWager

def generateTargetValue(numSpinners, spinnerLow, spinnerHigh):
    """ This function generates the 'target value' that the players try to match. This number is generated by summing together
    'numSpinners' number of spinners, between 'spinnerLow' and 'spinnerHigh' (inclusive). For example, of there are 3 spinners,
    and a spinner can have values between 1 - 3, then the target value would be the summation of 3 random values between 1 and 3
    (inclusive).

    :param Integer numSpinners: The number of spinners used in the game.
    :param Integer spinnerLow: The smallest value a spinner can generate.
    :param Integer spinnerHigh: The highest value a spinner can generate.
    :return Integer: The target value generated by summing together 'numSpinners' number of random numbers between 'spinnerLow' and
                     'spinnerHigh' (inclusive).
    """
    
    target = 0
   
    randNum = random.randint(1, numSpinners+1)

    for i in range(randNum):
        target += random.randint(spinnerLow, spinnerHigh)

    return target








def getSpinnerChoiceHuman(playerNum, target, numSpinners, spinnerLow, spinnerHigh):
    """ This function gets the number of spinners that the human wants to spin. It should print out the 'target value' that the player
    is trying to match, as well as the values that a spinner can produce (ex: 1 - 3), and the number of spinners that can be spun. The
    player cannot pick more spinners than are in the game, nor can the pick zero spinners, nor can they pick a negative number of spinners.

    :param Integer playerNum: The player to include in any printouts. Can be 1 or 2.
    :param Integer target: The 'target value' the player is trying to match.
    :param Integer numSpinners: The total number of spinners in the game.
    :param Integer spinnerLow: The smallest value a spinner can generate.
    :param Integer spinnerHigh: The highest value a spinner can generate.
    :return Integer: The number of spinners the player chooses to spin.
    """
    spinnerChoice = 0
   

    while True:
        spinnerChoice = int(input("How many spinners do you want to spin?"))
        if spinnerChoice<=numSpinners and spinnerChoice>0:
                break
        elif spinnerChoice>numSpinners or spinnerChoice<0:
            print("Please select another number")

    # NOTE: Use the spinnerLow and spinnerHigh values to output data about what the player could spin
    print("* Player {0} can spin {1} points up to {2} points. Target value: {3}".format(playerNum, spinnerLow, spinnerHigh, target))
    return spinnerChoice

def getSpinnerChoiceAI(playerNum, target, numSpinners, spinnerLow, spinnerHigh):
    """ This function gets the number of spinners that the computer wants to spin. This number should be a randomly generated value
    between 1 and numSpinners (inclusive). It should print out text similar to what the 'getSpinnerChoiceHuman()' function produces.

    :param Integer playerNum: The player to include in any printouts. While this will usually be 2, including this value allows
                              for a future version of the game with 2 computer players.
    :param Integer target: The 'target value' the computer is trying to match. The computer does not take this value into account when
                           choosing the number of spinners - it should be used for printouts, however.
    :param Integer numSpinners: The total number of spinners in the game.
    :param Integer spinnerLow: The smallest value a spinner can generate.
    :param Integer spinnerHigh: The highest value a spinner can generate.
    :return Integer: The number of spinners the computer chooses to spin.
    """
    spinnerChoice = 0
   

    spinnerChoice = random.randrange(1, (numSpinners+1))

    # NOTE: Try to have the AI version match the look of the output of the human version above
    print("* Player {0} can spin {1} points up to {2} points. Target value: {3}".format(playerNum, spinnerLow, spinnerHigh, target))
    return spinnerChoice

def spinSpinners(playerNum, spinnerChoice, target, spinnerLow, spinnerHigh):
    """ This function can be used for either human or computer players, and it calculates the summed values of the number of
    spinner spins. For example, if the player chooses to spin 3 spinners, and these spinners can have values between 1 and 3,
    the player could spin values of 2, 3, and 1 for a total of 6. This is the value the function would return.

    This function should print out the results of each spin as each spin is spun. The function should then print the sum of
    all the spins and the target value once all the spins are complete.

    Please note - the winner of the round is *not* calculated here - only the spinner totals.

    :param Integer playerNum: The player to include in any printouts. Can be 1 or 2.
    :param Integer spinnerChoice: The number of spinners the player wishes to spin.
    :param Integer target: Use this value in the printout so the user can compare what they spun compared to the target.
    :param Integer spinnerLow: The smallest value a spinner can generate.
    :param Integer spinnerHigh: The highest value a spinner can generate.
    :return Integer: The sum of all the spinner spins.
    """
    spinTotal = 0 

    for i in range(spinnerChoice):
        spinVal = random.randint(spinnerLow, spinnerHigh)
        spinTotal += spinVal 

        print(f"Player {playerNum} spun: {spinVal}")

    print(f"Player {playerNum} total spin value: {spinTotal}")
    print(f"Target value: {target}")

    return spinTotal


def main():
    """ This is the main function that executes when the game is started from the terminal. It contains all of the logic/ states
    necessary to play the game.
    """
    # main script running control variable
    running = True
   
    # gameplay variables
    SPINNER_LOW = 1
    SPINNER_HIGH = 3
    NUM_SPINNERS = 3
    INITIAL_POINTS = 10
    player1Points = INITIAL_POINTS
    player2Points = INITIAL_POINTS

    # print the title/ author information
    printTitleMaterial()

    # play the game
    while running:
        choice = initialChoice()
        if choice == "p":

            # numPlayers = chooseNumPlayers()

            # main game loop
            while True:
                # round setup
                printBanner()

               
                # NOTE: You can do this any way you like - in fact, you can even discard this file entirely and make your own version!
                #       The comments below provide a general outline of the overall 'algorithm' of the game
                #       Each comment could have multiple lines of code associated with it
                #       Your job is to iteratively build up a game that works - it doesn't really matter how you code it

                # Find a target value.
                target = str(generateTargetValue(NUM_SPINNERS, SPINNER_LOW, SPINNER_HIGH))
                print("The target value is: "+ target)

                # Player 1 wager. (Player 1 will always be human.)
                playerNum1 = 1

                
                choose = chooseNumPlayers()
               
                playerOneWager= wagerPointsHuman(playerNum1, player1Points)
                print("Player One Wager is: "+ str(playerOneWager))

                # Player 2 wager. (Player 2 can be either human or AI - you must account for both.)
               
                playerNum2 = 2

                if choose== 1:
                   
                   
                    playerTwoWager = wagerPointsAI(playerNum2, player2Points)
                    print("Player Two Wager is: "+ str(playerTwoWager))

                elif choose== 2:
                   
                   
                    playerTwoWager = wagerPointsHuman(playerNum2, player2Points)
                    print("Player Two Wager is: "+ str(playerTwoWager))

               
                   
                   
               
               
                # Player 1 spin - get the total of all the spinners. (Player 1 will always be human.)
                spinChoice1 = getSpinnerChoiceHuman(playerNum1, target, NUM_SPINNERS, SPINNER_LOW, SPINNER_HIGH)
                playerTotal1 = spinSpinners(playerNum1, spinChoice1, target, SPINNER_LOW, SPINNER_HIGH)

                # # Player 2 spin - get the total of all the spinners. (Player 2 can be either human or AI - you must account for both.)
                if choose== 1:
                    spinChoice2 = getSpinnerChoiceAI(playerNum2, target, NUM_SPINNERS, SPINNER_LOW, SPINNER_HIGH)
                   
                else:
                    spinChoice2 = getSpinnerChoiceHuman(playerNum2, target, NUM_SPINNERS, SPINNER_LOW, SPINNER_HIGH)
                   
                playerTotal2 = spinSpinners(playerNum2, spinChoice2, target, SPINNER_LOW, SPINNER_HIGH)

                
                # # Calculate Winner of the round. (If Player1 is closer, Player 1 wins. Else if Player 2 is closer Player 2 wins. If they are equal it is a draw.)
                if (int(target) - int(playerTotal1))>(int(target) - int(playerTotal2)):
                    print("Player Two Has Won!")
                elif (int(target) - int(playerTotal1))<(int(target) - int(playerTotal2)):
                    print("Player One Has Won!")
                else:
                    print("Tie!")
                # # Print the points for both players.
                print("Player One Had"+str(playerTotal1)+"points")
                print("Player Two Had"+str(playerTotal2)+"points")
                print("The round has ended.")
                if running == False:
                    print("Game Over")
                    playerTotal1 = 0
                    playerTotal2 = 0
                    break

                # Check of the game is over - if it is, print a 'game over' message, reset the points to default values,
                # and break out of the gameplay loop. Otherwise, print that it is the end of the round.

        elif choice == "i":
            print("The user selects a game between one or two players. ¡ A one player game has the user play against the computer. ¡ A two player game has two users play against one another. l The players start the game with a certain number of 'points.' l The game is divided into 'rounds.' ¡ At the start of the 'round,' the computer generates a 'target value.' n This is the value that the players try to match. ¡ At the start of the 'round,' each player 'wagers' a certain number of 'points.' n Players cannot 'wager' more 'points' than they have. Nor can they 'wager' zero (0) 'points.' Nor can they 'wager' a negative number of 'points.' ¡ Then each player decides how many 'spinners' to spin. n Each spin adds its value to a final 'spin value' for the round. n Players can not spin more spinners than are available to be spun. Nor can the spin zero (0) spinners. Nor can they spin a negative number of spinners. ¡ After each player spins their 'spinners,' their 'spin value' is compared against the 'target value.' ¡ The player who gets their 'spin value' closest to the 'target value' is the winner of the round. n If both players 'spin value' are equally distant from the 'target value' the round is a 'draw.' ¡ Once the winner is decided, 'points' are added and subtracted from each player's score. n The winning player gets their wager amount added to their score. n The losing player gets their wager amount subtracted from their score. ¡ The game continues until one player is completely out of 'points.'")
        elif choice == "q":
           
           
            print("Goodbye")
            running = False
           
        else:
            print("ERROR: Variable 'choice' should have been 'p', 'i', or 'q', but instead was:", choice)
            quit()

if __name__ == "__main__":
    main()