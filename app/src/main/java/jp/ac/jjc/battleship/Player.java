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

    Player(Board board) {
        this.board = board;
    }

    Board getBoard() {
        return board;
    }

    ShootResult shoot(Cell cellToShoot) {
        if(!cellToShoot.isHit()) {
            cellToShoot.hit();
            if(cellToShoot.hasShip()) {
                if(cellToShoot.getShip().isSunk()) {
                    cellToShoot.getShip().sunk();
                    board.incrNumOfShipsSunk();
                    if(board.areAllShipsSunk()) {
                        return ShootResult.END;
                    } else {
                        return ShootResult.KILL;
                    }
                } else {
                    return ShootResult.HIT;
                }
            }
            return ShootResult.MISS;
        }
        return ShootResult.FALSE;
    }

}
