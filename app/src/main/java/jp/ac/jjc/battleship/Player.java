package jp.ac.jjc.battleship;
enum ShootResult {
    HIT,
    MISS,
    KILL,
    END,
    FALSE
}
    class Player {
    Board board;
    GamePlayActivity.GamePlay game;
    ShootResult lastShoot = null;

    Player(Board board, GamePlayActivity.GamePlay game) {
        this.board = board;
        this.game = game;
    }

    Board getBoard() {
        return board;
    }


    ShootResult shoot(Cell cellToShoot) {
        if(!cellToShoot.isHit()) {
            game.playSoundEffect("FIRE");
            cellToShoot.hit();
            if(cellToShoot.hasShip()) {
                game.playSoundEffect("HIT");
                if(cellToShoot.getShip().isSunk()) {
                    cellToShoot.getShip().sink();
                    board.incrNumOfShipsSunk();
                    if(board.areAllShipsSunk()) {
                        lastShoot = ShootResult.END;
                    } else {
                        lastShoot = ShootResult.KILL;
                    }
                } else {
                    lastShoot = ShootResult.HIT;
                }
            } else {
                game.playSoundEffect("MISS");
                lastShoot = ShootResult.MISS;
            }
        } else {
            lastShoot = null;
        }
        return lastShoot;
    }

}
