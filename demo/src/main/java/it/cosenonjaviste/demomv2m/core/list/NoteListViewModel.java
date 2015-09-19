package it.cosenonjaviste.demomv2m.core.list;

import android.databinding.ObservableBoolean;

import java.util.List;
import java.util.concurrent.Executor;

import it.cosenonjaviste.demomv2m.core.Navigator;
import it.cosenonjaviste.demomv2m.core.detail.NoteModel;
import it.cosenonjaviste.demomv2m.model.Note;
import it.cosenonjaviste.demomv2m.model.NoteLoaderService;
import it.cosenonjaviste.mv2m.ViewModel;

public class NoteListViewModel extends ViewModel<NoteListModel> {

    private NoteLoaderService service;

    private ObservableBoolean loading = new ObservableBoolean();

    private final Executor backgroundExecutor;

    private final Executor uiExecutor;

    private Navigator navigator;

    public NoteListViewModel(NoteLoaderService service, Executor backgroundExecutor, Executor uiExecutor, Navigator navigator) {
        this.service = service;
        this.backgroundExecutor = backgroundExecutor;
        this.uiExecutor = uiExecutor;
        this.navigator = navigator;
        registerActivityAware(navigator);
    }

    @Override public NoteListModel createDefaultModel() {
        return new NoteListModel();
    }

    @Override public void resume() {
        if (!loading.get() && !getModel().isLoaded()) {
            reloadData();
        }
    }

    public void reloadData() {
        loading.set(true);
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                try {
                    final List<Note> notes = service.loadItems().getResults();
                    uiExecutor.execute(new Runnable() {
                        @Override public void run() {
                            loading.set(false);
                            getModel().loadedData(notes);
                        }
                    });
                } catch (Exception e) {
                    uiExecutor.execute(new Runnable() {
                        @Override public void run() {
                            loading.set(false);
                            getModel().loadedWithError();
                        }
                    });
                }
            }
        });
    }

    public ObservableBoolean getLoading() {
        return loading;
    }

    public void openDetail(Note note) {
        navigator.openDetail(new NoteModel(note.getObjectId()));
    }
}