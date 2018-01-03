# PaginationViewModel

This library is a wrapper around the new Android's Architecture component [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) with extended functionality and a few major difference, to understand a little bit better these differences let's look at how Android's [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) works compared to [PaginationViewModel](https://github.com/MartinCazares/delta-viewmodel/blob/master/app/src/main/java/com/doepiccoding/viewmodel/models/PaginationViewModel.java).

In a nutshell as per documentation "The [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) class is designed to store and manage UI-related data in a lifecycle conscious way. The [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) class allows data to survive configuration changes such as screen rotations."

The very same principle applies to [PaginationViewModel](https://github.com/MartinCazares/delta-viewmodel/blob/master/app/src/main/java/com/doepiccoding/viewmodel/models/PaginationViewModel.java), hence, under the hood both of them are essentially the same, however, the current [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) implementation works perfect for static data and immutable collections, as shown in the documentation's example, you could fetch once a list of users that takes a while to load and then it will be available for you after configuration changes without having to load it again:

    public class MyViewModel extends ViewModel {
        private MutableLiveData<List<User>> users;
        public LiveData<List<User>> getUsers() {
            if (users == null) {
                users = new MutableLiveData<List<Users>>();
                loadUsers();
            }
            return users;
        }

        private void loadUsers() {
            // Do an asyncronous operation to fetch users.
        }
    }
And then you can start observing this source of data, so that if anything changes your UI will get notified:

    MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);
                model.getUsers().observe(this, users -> {
                    // update UI
                });
However, as mentioned before this approach works perfectly **only** for immutable lists, because it doesn't have an append or subset of objects option, for example, with ViewModel, if you have a list like this:

 - Angy N
 - Beatty M
 - Chris L

Assuming that this is a pagination list and now you fetch the next page of users and add three more elements to the observed list "David W, Edgar H, Felicia P",  the list updated will return in the callback the following items:

 - Angy N
 - Beatty M
 - Chris L
 - David W
 - Edgar H
 - Felicia P

This approach makes it really hard to use this data in a RecyclerView because you would have to either reload every single element in the RecyclerView or come up with your own mechanism to know which elements are already in and which ones are new, so you can notify the adapter to add your newly added elements.

This is where the [PaginationViewModel](https://github.com/MartinCazares/delta-viewmodel/blob/master/app/src/main/java/com/doepiccoding/viewmodel/models/PaginationViewModel.java) comes to play, this wrapper class doesn't contain one single "onChanged" method, but instead provides "DeltaMethods":

    public interface PaginationObserver<T> {
            void onNewPage(@Nullable List<T> pageSubsetOnly, String pageId);
            void onReloaded(@Nullable List<T> entireData);
            void onReset();
        }
Now, based on the previous example, assuming that you already loaded the first page of users:
 - Angy N
 - Beatty M
 - Chris L

If you fetch the next page through [PaginationViewModel](https://github.com/MartinCazares/delta-viewmodel/blob/master/app/src/main/java/com/doepiccoding/viewmodel/models/PaginationViewModel.java), in the method
> void onNewPage(@Nullable List<T> t, String pageId);

You will get only these elements:
- David W
 - Edgar H
 - Felicia P

Making it extremely easy to use with RecyclerView and also getting all the benefits mentioned on the ViewModel, because since the wrapper class is activity lifecycle aware, when there's a configuration change you would get a call back on
> void onReloaded(@Nullable List<T> entireData);

With all the elements you previously had without having to reload them, making the synchronization with the RecyclerView as simple as this:

		val paginationObserver = object: PaginationViewModel.PaginationObserver<String> {

                override fun onNewPage(pageSubsetOnly: MutableList<String>?, pageId: String) {
                    populatePage(pageSubsetOnly)
                }

                override fun onReset() {
                    pagesAdapter.removeAllItems()
                }

                override fun onReloaded(entireData: MutableList<String>?) {
                    populatePage(entireData)
                }

            }

		//Setup Model...
		model = ViewModelProviders.of(this).get(MyPaginationModel::class.java)
		model.observePagination(object: PaginationViewModel.PaginationSubscriber{
                override fun getLifecycleOwner(): LifecycleOwner = this@PaginationActivity
                override fun getPaginationObserver(): PaginationViewModel.PaginationObserver<String>  = paginationObserver
            })

New components will be added, to manipulate not only pagination cases, but also other common cases that need to be life cycle aware, any suggestion for cases to cover under the umbrella of ViewModel are welcome.