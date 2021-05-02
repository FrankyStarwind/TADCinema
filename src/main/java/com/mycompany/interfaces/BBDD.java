package com.mycompany.interfaces;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;

public class BBDD {
    
    private DBCollection coleccion;
    
    public BBDD(String tabla) throws UnknownHostException {
        final MongoClient mongoClient = new MongoClient("localhost", 27017);
        final DB db = mongoClient.getDB("TADCinemaDB");
        setColeccion(db.getCollection(tabla));
    }

    public DBCollection getColeccion() {
        return coleccion;
    }

    public void setColeccion(DBCollection coleccion) {
        this.coleccion = coleccion;
    }
    
}
