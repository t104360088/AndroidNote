package com.example.myroom

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.text.method.MovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var items: ArrayList<String> = ArrayList()
    private val handler = Handler { msg ->
        when (msg.what) {
            1 -> adapter.notifyDataSetChanged()
        }
        showProgress(false)
        true
    }
    private lateinit var dao: UserEntityDao
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = AppDatabase.shared(this).getDao()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        btnAdd.setOnClickListener(this)
        btnRevise.setOnClickListener(this)
        btnDelete.setOnClickListener(this)
        btnQuery.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        showProgress(true)

        val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)

        when (view.id) {
            R.id.btnAdd -> addUser()
            R.id.btnRevise -> reviseUser()
            R.id.btnDelete -> deleteUser()
            R.id.btnQuery -> queryUser()
        }
    }

    //MARK: ButtonAction
    private fun addUser() {

        val name = editName.text.toString()
        val age = editAge.text.toString()
        if (!name.isEmpty() && !age.isEmpty()) {
            AsyncTask.execute {
                val user = UserEntity(name, age.toInt())
                dao.addUser(user)
                clearField()
                queryUser()
            }
        } else {
            showToast("請輸入完整資料")
            showProgress(false)
        }
    }

    private fun reviseUser() {

        val name = editName.text.toString()
        val age = editAge.text.toString()
        if (!name.isEmpty() && !age.isEmpty()) {
            AsyncTask.execute {
                val user = UserEntity(name, age.toInt())
                val count = dao.updateUser(user)
                if (count == 0) {
                    showToast("無此用戶")
                    showProgress(false)
                } else {
                    showToast("已完成修改")
                    clearField()
                    queryUser()
                }
            }
        } else {
            showToast("請輸入完整資料")
            showProgress(false)
        }
    }

    private fun deleteUser() {
        val name = editName.text.toString()
        if (!name.isEmpty()) {
            AsyncTask.execute {
                val user = dao.queryByName(name)
                if (user != null) {
                    dao.deleteUser(user)
                    clearField()
                    queryUser()
                    showToast("已刪除")
                } else {
                    showToast("無此用戶")
                    showProgress(false)
                }
            }
        } else {
            showToast("請輸入用戶名稱")
            showProgress(false)
        }
    }

    private fun queryUser() {
        val name = editName.text.toString()
        items.clear()

        AsyncTask.execute {
            if (!name.isEmpty()) {
                val user = dao.queryByName(name)
                if (user != null) {
                    addItem(user)
                }
            } else {
                for (user in dao.getAll()) {
                    addItem(user)
                }
            }
            sendMsg()
        }
    }

    //MARK: Action
    private fun addItem(user: UserEntity) {
        val userName = user.name
        val userAge = user.age
        val item = userName + "\t\t\t${userAge}歲"
        items.add(item)
    }

    private fun sendMsg() {
        val msg = Message()
        msg.what = 1
        handler.sendMessage(msg)
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgress(isEnable: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (isEnable) View.VISIBLE else View.INVISIBLE
        }
    }

    //MARK: Layout
    private fun clearField() {
        editName.setText("")
        editAge.setText("")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(null != this.currentFocus){
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }
}
