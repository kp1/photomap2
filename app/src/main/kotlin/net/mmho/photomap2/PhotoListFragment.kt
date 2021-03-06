package net.mmho.photomap2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.core.view.MenuItemCompat
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_photo_list.view.*
import java.util.*

class PhotoListFragment : Fragment() {

    private var adapter: PhotoListAdapter? = null
    private var photoList: ArrayList<HashedPhoto>? = null
    private var newest = true
    private var distanceIndex: Int = 0

    // rxAndroid
    private var disposable: Disposable? = null
    private var subject: PublishSubject<Int>? = null
    private var permissionGranted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)

        photoList = ArrayList()
        adapter = PhotoListAdapter(requireContext(), R.layout.layout_photo_card, ArrayList())
        when(savedInstanceState) {
            null -> {
                distanceIndex = DistanceActionProvider.initialIndex()
            }
            else -> {
                distanceIndex = savedInstanceState.getInt("DISTANCE")
                requireActivity().title = savedInstanceState.getString("title")
            }
        }
        subject = PublishSubject.create<Int>()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PhotoListActivity.PERMISSIONS_REQUEST)
            } else {
                view?.let {
                    PermissionUtils.requestPermission(it, requireContext())
                }
            }
        } else {
            grantedPermission(true)
        }
    }

    override fun onStart() {
        super.onStart()
        if (disposable == null)
            disposable = subject?.switchMap { distance -> groupObservable(distance) }?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        disposable = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.photo_list_menu, menu)

        val distance = menu.findItem(R.id.distance)
        val distanceActionProvider = MenuItemCompat.getActionProvider(distance) as DistanceActionProvider
        distanceActionProvider.setDistanceIndex(distanceIndex)
        distanceActionProvider.setOnDistanceChangeListener(object : DistanceActionProvider.OnDistanceChangeListener {
            override fun onDistanceChange(index: Int) {
                distanceIndex = index
                subject?.onNext(index)
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.oldest -> {
                newest = false
                LoaderManager.getInstance(this)
                    .restartLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks)
            }
            R.id.newest -> {
                newest = true
                LoaderManager.getInstance(this)
                    .restartLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks)
            }
            R.id.about -> {
                val i = Intent(activity, AboutActivity::class.java)
                startActivity(i)
            }
            else -> {}
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when (permissionGranted){
            true ->{
                menu.findItem(R.id.newest).isEnabled = !newest
                menu.findItem(R.id.oldest).isEnabled = newest
                menu.findItem(R.id.distance).isEnabled = true
            }
            else -> {
                menu.findItem(R.id.newest).isEnabled = false
                menu.findItem(R.id.oldest).isEnabled = false
                menu.findItem(R.id.distance).isEnabled = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_thumbnail, container, false)

    private val onItemClickListener = AdapterView.OnItemClickListener{
        _, _, position, _ ->
        val group = adapter?.getItem(position)
        val intent: Intent
        when(group?.size){
            null -> return@OnItemClickListener
            1 ->{
                intent = Intent(activity, PhotoViewActivity::class.java)
                intent.putExtra(PhotoViewActivity.EXTRA_GROUP, group as Parcelable)
            }
            else->{
                intent = Intent(activity, ThumbnailActivity::class.java)
                intent.putExtra(ThumbnailActivity.EXTRA_GROUP, group as Parcelable)
            }
        }
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // photo list
        val list = view.thumbnail_grid as GridView
        list.adapter = adapter
        list.onItemClickListener = onItemClickListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("DISTANCE", distanceIndex)
        outState.putString("title", activity?.title.toString())
    }

    private var order :String? = null
    private val photoCursorCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            val q = QueryBuilder.createQuery()  // all list
            val o = if (newest) QueryBuilder.sortDateNewest() else QueryBuilder.sortDateOldest()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return CursorLoader(requireContext(), uri, PhotoCursor.projection, q, null, o)
        }
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            val newOrder = (loader as CursorLoader).sortOrder
            if(order!=newOrder || photoList?.size!=data.count) {
                order = newOrder
                photoList = PhotoCursor(data).hashedPhotoList
                subject?.onNext(distanceIndex)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
        }
    }

    private fun groupObservable(distance: Int): Observable<List<PhotoGroup>> {
        val length =  DistanceActionProvider.getDistance(distance)
        val older = order == QueryBuilder.sortDateOldest()
        return Flowable.fromIterable(photoList)
            .subscribeOn(Schedulers.newThread())
            .groupBy { hash -> hash.hash.toBase32().substring(0,length) }
            .flatMapSingle { it.map(::PhotoGroup).reduce(PhotoGroup::append).toSingle() }
            .toSortedList{ g1,g2 ->
                if(older)  g1.dateTaken.compareTo(g2.dateTaken)
                else g2.dateTaken.compareTo(g1.dateTaken)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { adapter?.clear() }
            .doOnSuccess { list -> adapter?.addAll(list) }
            .toObservable()
    }

    fun grantedPermission(granted: Boolean) {
        if (granted) {
            LoaderManager.getInstance(this)
                .initLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks)
        } else {
            view?.let {
                PermissionUtils.requestPermission(it, requireContext())
            }
        }
    }

    companion object {
        private const val CURSOR_LOADER_ID = 0
    }
}
