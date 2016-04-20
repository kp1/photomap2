package net.mmho.photomap2

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_thumbnail_list.*

class ThumbnailActivity : AppCompatActivity() {
    private var fragment: ThumbnailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thumbnail_list)

        val bundle = intent.extras
        when (bundle) {
            null -> {
                finish()
            }
            else -> {
                fragment = supportFragmentManager.findFragmentById(R.id.list) as ThumbnailFragment
                fragment?.setList(bundle.getParcelable<PhotoGroup>(EXTRA_GROUP))
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragment?.setPosition(data?.extras?.getInt(PhotoViewActivity.EXTRA_GROUP) ?: 0)
    }

    companion object {
        val EXTRA_GROUP = "thumbnail_group"
    }
}