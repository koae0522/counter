package com.example.counter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.counter.ui.theme.CounterTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Switch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CounterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CounterApp( context = LocalContext.current)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterApp(itemViewModel:ItemViewModel= viewModel(),
               totalViewModel: TotalViewModel = viewModel(),
               context: Context
) {

    val itemUiState by itemViewModel.uiState.collectAsState()
    val totalUiState by totalViewModel.uiState.collectAsState()

    var addItemButtonClicked by remember {mutableStateOf(false)}
    var deleteAllItemButtonClicked by remember {mutableStateOf(false)}
    var loaded by remember {mutableStateOf(false)}
    val checkedState = remember { mutableStateOf(false) }

    var db = ItemDatabase.getDatabase(context)
    var dao = db.itemDao()
    val compositeDisposable = CompositeDisposable()

    dao.getAll()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            if(!loaded) {
                itemViewModel.loadItem(it.toMutableList())
                var totalValue = 0
                for (i in it) {
                    totalValue += i.price * i.number
                    runBlocking {
                        totalViewModel.loadTotalValue(totalValue)
                    }
                }
                loaded = true
            }

        }.addTo(compositeDisposable)


    //初期化する場合はOfをつける

    Box(){

            Scaffold (
                topBar ={
                    TopAppBar(
                        title ={Text("合計金額計算アプリ")},
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        actions = {
                            IconButton(onClick = {
                              checkedState.value=!checkedState.value
                            }) {
                                if(checkedState.value) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_visibility_off_24),
                                        contentDescription = null
                                    )
                                }else{
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_visibility_24),
                                        contentDescription = null
                                    )
                                }
                            }
                            IconButton(onClick = {
                                deleteAllItemButtonClicked=true
                            }) {
                                Icon(painter = painterResource(R.drawable.baseline_delete_24),contentDescription = null)
                            }
                        }
                    )

                },
                bottomBar = {
                    BottomAppBar() {
                        if(checkedState.value) {
                            Text(
                                "合計" + totalUiState.toString() + "円",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }else{
                            Text(
                                "合計***円",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { addItemButtonClicked = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

            ){
                    innerPadding ->
                Column(modifier = Modifier.fillMaxWidth()){

                    ItemListColumn(innerPadding,itemUiState,dao,db,compositeDisposable)

                }

            }
        }


    if(deleteAllItemButtonClicked) {
        var text by remember {
            mutableStateOf("データを消去します。\n" + "よろしいですか？")}

        AlertDialog(
            onDismissRequest = {
                deleteAllItemButtonClicked= false
            },
            confirmButton = {
                TextButton(onClick = {
                    Completable.fromAction { dao.deleteAll() }
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                        .addTo(compositeDisposable)
                    text="データを消去しました。\n再起動してください。"
                },

                ) {
                    Text("消去する")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleteAllItemButtonClicked = false
                }){
                    Text("キャンセル")
                }
            },
            title = {
                Text("確認")
            },
            text = {
                Text(text)
            }
        )
    }

        if(addItemButtonClicked) {

            var itemName by remember{mutableStateOf("")}
            var itemValue by remember{mutableStateOf("")}

            AlertDialog(
                onDismissRequest = {
                addItemButtonClicked=false
                },
                confirmButton = {
                    TextButton(onClick = {
                        addItemButtonClicked = false
                        runBlocking {
                            itemViewModel.addItem(Item(0,itemName,itemValue.toInt(),1))
                            totalViewModel.changeTotalValue(itemValue.toInt(),true)
                        }
                        Completable.fromAction{
                            dao.insert(Item(0,itemName,itemValue.toInt(),1))
                        }.subscribeOn(Schedulers.io())
                            .subscribe()
                            .addTo(compositeDisposable)
                    },
                        enabled = itemValue!=""
                    ) {
                        Text("追加")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        addItemButtonClicked = false
                    }){
                        Text("キャンセル")
                    }
                },
                title = {
                    Text("アイテムを追加")
                },
                text = {
                    Column(){
                        Text("名前")
                        OutlinedTextField(value = itemName, onValueChange = {itemName = it})
                        
                        Text("価格")
                        OutlinedTextField(value = itemValue, onValueChange = {itemValue = it},
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                            )
                    }
                }
                ) 
        }
    }




@Composable
fun ItemListColumn(innerPadding:PaddingValues,itemUiState:List<Item>,dao: ItemDao,db:ItemDatabase,compositeDisposable:CompositeDisposable){
    LazyColumn(modifier= Modifier
        .padding(innerPadding)
        .padding(10.dp)){
        itemsIndexed(itemUiState) { index, item ->
            ItemRow(item.id,item.name, item.price, item.number, index,dao,db,compositeDisposable)
            Divider(modifier = Modifier.padding(5.dp))
        }
    }
}


@Composable
fun ItemRow(id:Int,name:String,price:Int,number:Int,index:Int,
            dao: ItemDao,db:ItemDatabase,compositeDisposable:CompositeDisposable,
            itemViewModel:ItemViewModel= viewModel(),totalViewModel: TotalViewModel=viewModel(),
           ){
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.Center){
        Text(name,Modifier.weight(1f),style = MaterialTheme.typography.bodyLarge)
        Text(price.toString()+"円",Modifier.weight(1f),style = MaterialTheme.typography.bodyLarge)
        FilledTonalButton(onClick = {
            runBlocking {
                itemViewModel.changeNumber(index, true)
                totalViewModel.changeTotalValue(price, true)
            }
            Completable.fromAction {
                dao.updateItem(Item(id, name, price, number + 1))
            }.subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compositeDisposable)
        }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
        Text(number.toString(),modifier =Modifier.padding(horizontal = 10.dp),style = MaterialTheme.typography.bodyLarge)
        FilledTonalButton(
            onClick = {
                runBlocking {
            itemViewModel.changeNumber(index,false)
            totalViewModel.changeTotalValue(price,false)
        }
                Completable.fromAction{
                    dao.updateItem(Item(id,name,price, number-1))
                }.subscribeOn(Schedulers.io())
                    .subscribe()
                    .addTo(compositeDisposable)
                      }, enabled = number>0
        ) {
            Icon(painter = painterResource(R.drawable.baseline_remove_24),contentDescription = null)
        }
    }
}





