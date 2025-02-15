package com.integration.service;

import com.integration.dao.PlayerDao;
import com.integration.dto.Player;
import com.integration.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PlayerService {

    private final PlayerDao playerDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

    private final PublishKafkaService publishKafkaService2;

    @Autowired
    public PlayerService(PlayerDao playerDao, PublishKafkaService publishKafkaService2){
        this.playerDao = playerDao;
        this.publishKafkaService2 = publishKafkaService2;
    }

    // TODO: Lookup before creating
    public Response createPlayer(Player player){

        try{
            Player existingPlayer = playerDao.findByKey(player.generateKey());
            if(existingPlayer == null){
                playerDao.save(player);
                publishKafkaService2.sendMessage("players2", player);
            }else{
                LOGGER.info("Player with key={} already existed", existingPlayer.getKey());
                return new Response(HttpStatus.OK,"Player Created Previously");
            }

        }catch (Exception e){
            return new Response(HttpStatus.BAD_REQUEST,"Error Creating Entity, error="+e.getMessage());
        }

        return new Response(HttpStatus.OK,"Success");
    }

    /*
        Function only updates number
     */
    public Response updatePlayer(Player player){
        try{
            Player savedPlayer = playerDao.findByKey(player.generateKey());
            if(savedPlayer != null) {
                savedPlayer.setNumber(player.getNumber());
                playerDao.save(savedPlayer);
            }else{
                return new Response(HttpStatus.BAD_REQUEST,"Error Updating Entity, the player you tried to update does not exist");
            }

        }catch (Exception e){
            return new Response(HttpStatus.BAD_REQUEST,"Error Updating Entity, error="+e.getMessage());
        }

        return new Response(HttpStatus.OK,"Success");
    }
}
