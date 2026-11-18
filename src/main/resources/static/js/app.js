"use strict";

const mainTab = document.querySelectorAll('.main-tab');
const mainContainer = document.querySelector('#inventory-content-container');

const icons = document.querySelectorAll('.icon-option');
const iconModal = document.querySelector('#iconModal');
const previewImg = document.querySelector('#preview');
const iconId = document.querySelector('#iconId');

const categoryTab = document.querySelectorAll('.category-tab');
const container = document.querySelector('#product-list-container');
const alertsTab = document.querySelectorAll('.alert-tab');

icons.forEach((icon) => {
  icon.addEventListener('click', () => {
    previewImg.src = icon.dataset.path;
    iconId.value = icon.dataset.id;

    bootstrap.Modal.getOrCreateInstance(iconModal).hide();
  })
})

// 在庫一覧のタブ切り替え
categoryTab.forEach((category) => {
  category.addEventListener('click', async (event) => {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const categoryId = category.dataset.categoryId;
    const params = new URLSearchParams({
      keyword: keyword,
      categoryId: categoryId
    });

    const url = `/inventory/list/fragment?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    container.innerHTML = html;

    categoryTab.forEach((tab) => {
      tab.classList.remove('active');
    });
    category.classList.add('active');
  });
})

// 在庫、期限切れのタブ切り替え
alertsTab.forEach((alert) => {
  alert.addEventListener('click', async (event) => {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const alertType = alert.dataset.type;
    const params = new URLSearchParams({
      keyword: keyword,
      type: alertType
    });

    const url = `/inventory/alerts/fragment?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    container.innerHTML = html;

    alertsTab.forEach((tab) => {
      tab.classList.remove('active');
    });
    alert.classList.add('active');
  });
})


// 一覧、期限の上位タブ切り替え
mainTab.forEach((mainTabEl) => {
  mainTabEl.addEventListener('click', async (event) => {
    event.preventDefault()
    const tab = mainTabEl.dataset.tab;
    const keyword = document.querySelector('input[name="keyword"]').value
    const params = new URLSearchParams({keyword: keyword,});

    const url = `/inventory/${tab}/content?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    mainContainer.innerHTML = html;

    mainTab.forEach((tab) => {
      tab.classList.remove('active');
    });
    mainTabEl.classList.add('active');
    const form = document.querySelector('form');
    form.action = `/inventory/${tab}`;

    const hiddenInput = form.querySelector('input[type="hidden"]')
    if(hiddenInput) {
      hiddenInput.remove();
    }
  });
})

// メインタブ切り替え時に下位のタブ切り替えで再度読み込みを無くす
if (mainContainer) {
  mainContainer.addEventListener('click', async (event) => {
  const categoryTabEl = event.target.closest('.category-tab')
  const alertTabEl = event.target.closest('.alert-tab')

  if(categoryTabEl) {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const categoryId = categoryTabEl.dataset.categoryId;
    const params = new URLSearchParams({
      keyword: keyword,
      categoryId: categoryId
    });
    const url = `/inventory/list/fragment?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    document.querySelector('#product-list-container').innerHTML = html;
    document.querySelectorAll('.category-tab').forEach((tab) => {
      tab.classList.remove('active');
    });
    categoryTabEl.classList.add('active');
    }else if(alertTabEl) {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const alertType = alertTabEl.dataset.type;
    const params = new URLSearchParams({
      keyword: keyword,
      type: alertType
    });
    const url = `/inventory/alerts/fragment?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    document.querySelector('#product-list-container').innerHTML = html;
    document.querySelectorAll('.alert-tab').forEach((tab) => {
      tab.classList.remove('active');
    });
    alertTabEl.classList.add('active');
    }
  })
}


// 【追加】買い物リスト：カテゴリーIDで商品候補を絞り込む
const categoryFilter = document.querySelector('#categoryFilter');
const productSelect = document.querySelector('#productId');

// 買い物リストの入力欄がある画面だけで実行する
if (categoryFilter && productSelect) {
  // 「すべて」に戻せるように、最初の全選択肢を保存する
  const allProductOptions = Array.from(productSelect.options);
  function filterShoppingProducts() {
    const categoryId = categoryFilter.value;
    const selectedProductId = productSelect.value;

    // 現在の商品候補を空にし、条件に合う選択肢を入れ直す
    productSelect.replaceChildren();
    allProductOptions.forEach((option) => {
      const isPlaceholder = option.value === '';
      const isAllCategories = categoryId === 'すべて';
      const matchesCategory = option.dataset.category === categoryId;

      // 案内用の選択肢と、選択カテゴリーに該当する商品を表示する
      if (isPlaceholder || isAllCategories || matchesCategory) {
        productSelect.appendChild(option.cloneNode(true));
      }
    });

    // 選択中の商品が、絞り込み後も表示されているか確認する
    const visibleOptions = Array.from(productSelect.options);
    const selectionExists = visibleOptions.some((option) => {
      return option.value === selectedProductId;
    });

    // 対象の商品なら選択を維持し、対象外なら解除する
    if (selectionExists) {
      productSelect.value = selectedProductId;
    } else {
      productSelect.value = '';
    }
  }

  // カテゴリー変更時と初期表示時に、商品候補を絞り込む
  categoryFilter.addEventListener('change', filterShoppingProducts);
  filterShoppingProducts();
}


// 【追加】買い物リスト：チェック状態の保存・削除・購入完了
const shoppingChecks = document.querySelectorAll('.shopping-check');
const shoppingActionForms = document.querySelectorAll('.shopping-action-form');

if (shoppingChecks.length > 0 || shoppingActionForms.length > 0) {
  // 保存中の通信と、処理の状態を管理する
  const pendingCheckUpdates = new Set();
  let checkSaveFailed = false;
  let shoppingActionSubmitting = false;

  // ① チェック更新をControllerへ送信し、保存結果を確認する
  async function saveShoppingCheck(checkbox) {
    try {
      const response = await fetch(checkbox.dataset.url, {
        method: 'POST'
      });

      // HTTPエラーを確認する
      if (!response.ok) {
        throw new Error(`チェック更新に失敗しました（HTTP ${response.status}）`);
      }

      // Controllerから成功の応答が返ったか確認する
      const result = (await response.text()).trim();
      if (result !== 'success') {
        console.error('チェック更新の応答:', result);
        throw new Error('Controllerからsuccess以外の応答が返りました');
      }

    } catch (error) {
      // 保存結果が不明なため、再送せず再読み込みを案内する
      checkSaveFailed = true;
      console.error('買い物リストのチェック更新エラー:', error);
      window.alert(
        'チェック状態の保存を確認できませんでした。'
        + '画面を再読み込みして、チェック状態を確認してください。'
      );

    } finally {
      // 保存に成功し、フォーム送信中でもなければ再び操作できるようにする
      if (!checkSaveFailed && !shoppingActionSubmitting) {
        checkbox.disabled = false;
      }
    }
  }


  // ② チェックが変更されたら保存する
  shoppingChecks.forEach((checkbox) => {
    checkbox.addEventListener('change', () => {

      // 保存失敗後やフォーム送信中は、チェック変更を受け付けない
      if (checkSaveFailed || shoppingActionSubmitting) {
        checkbox.checked = !checkbox.checked;
        return;
      }

      // Controllerは状態を反転するため、通信中の連打を防ぐ
      checkbox.disabled = true;

      // 保存処理を開始し、保存待ちの一覧に追加する
      const update = saveShoppingCheck(checkbox);
      pendingCheckUpdates.add(update);

      // 完了した通信を、保存待ちの一覧から取り除く
      update.then(() => {
        pendingCheckUpdates.delete(update);
      });
    });
  });


    // ③ チェック保存が終わってから、削除・購入完了を直接送信する
  async function submitShoppingAction(event) {
    event.preventDefault();

    const form = event.currentTarget;

    // 削除・購入完了の二重送信を防ぐ
    if (shoppingActionSubmitting) {
      return;
    }

    // チェック保存に失敗している場合は送信しない
    if (checkSaveFailed) {
      window.alert('画面を再読み込みして、チェック状態を確認してください。');
      return;
    }

    // 【追加】チェックが1件もない場合は案内を表示する
    const hasCheckedItem = Array.from(shoppingChecks).some((checkbox) => {
      return checkbox.checked;
    });

    if (!hasCheckedItem) {
      window.alert('該当する商品にチェックを入れてください。');
      return;
    }

    shoppingActionSubmitting = true;

    // 保存待ちの途中でチェック状態が変更されないようにする
    shoppingChecks.forEach((checkbox) => {
      checkbox.disabled = true;
    });

    // チェック状態の保存がすべて完了するまで待つ
    await Promise.all(Array.from(pendingCheckUpdates));

    // 待機中に保存失敗が判明した場合も、送信しない
    if (checkSaveFailed) {
      shoppingActionSubmitting = false;
      return;
    }

    // submitイベントを再発生させず、フォームを直接送信する
    HTMLFormElement.prototype.submit.call(form);
  }

  // 削除・購入完了フォームに、送信前の確認処理を登録する
  shoppingActionForms.forEach((form) => {
    form.addEventListener('submit', submitShoppingAction);
  });
}