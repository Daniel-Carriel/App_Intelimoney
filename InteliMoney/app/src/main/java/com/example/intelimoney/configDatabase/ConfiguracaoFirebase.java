package com.example.intelimoney.configDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {

    /*
        Criar um atributo statico (static) faz com que o valor dele
        seja sempre o mesmo em todas as instancias que forem feitas
    */

    private static FirebaseAuth autenticacao;
    private static DatabaseReference firebase;

    // Retorna a instancia do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase(){
        if(firebase == null){
            firebase = FirebaseDatabase.getInstance().getReference();
        }
        return firebase;
    }

    // Retorna a instancia do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }
}
