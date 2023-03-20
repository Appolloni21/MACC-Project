package com.example.macc


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.TravelAdapter
import com.example.macc.model.Travel
import com.example.macc.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private const val TAG = "Homepage Fragment"

class Homepage : Fragment() {

    private lateinit var database : DatabaseReference
    private lateinit var recyclerView : RecyclerView
    private lateinit var travelArrayList : ArrayList<Travel>
    private var userTravelsSet: Set<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.homepage, container,
            false)



        // Initialize data.
        travelArrayList = arrayListOf<Travel>()
        getTravels()

        recyclerView = view.findViewById(R.id.recycler_view)
        //val activity = activity as Context

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addTravel -> {
                    val action = HomepageDirections.actionHomepageToAddTravel()
                    view.findNavController().navigate(action)
                }
            }
            true
        }
    }

     private fun getTravels(){

         val userUid = Firebase.auth.currentUser?.uid.toString()
         database = Firebase.database.getReference("users")
         userTravelsSet = setOf()

         //Prima di tutto prendiamo i riferimenti ai travel fatti dall'utente da dentro l'utente
         database.child(userUid).get().addOnSuccessListener {
             val user = it.getValue(User::class.java)
             userTravelsSet = user?.trips?.keys
             //Log.i(TAG, "viaggi dell'utente:${userTravelsSet}")

             //Poi dall'elenco dei travel nel db selezioniamo solo quelli che matchano con i travel effettivamente fatti dall'utente
             database = Firebase.database.getReference("travels")
             database.addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot : DataSnapshot) {
                     if(snapshot.exists()){
                         for(travelSnapshot in snapshot.children){
                             val travelKey = travelSnapshot.key.toString()
                             val travel = travelSnapshot.getValue(Travel::class.java)
                             if(userTravelsSet!!.contains(travelKey)){
                                 travelArrayList.add(travel!!)
                             }
                         }
                         recyclerView.adapter = TravelAdapter(travelArrayList)
                     }
                 }
                 override fun onCancelled(databaseError: DatabaseError) {
                     Log.w(TAG, "getTravels:onCancelled", databaseError.toException())

                 }
             })

         }.addOnFailureListener{
             Log.e(TAG, "Error getting data", it)
         }

    }
}