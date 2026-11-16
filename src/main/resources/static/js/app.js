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
mainTab.forEach((main) => {
  main.addEventListener('click', async (event) => {
    event.preventDefault()
    const tab = main.dataset.tab;
    const keyword = document.querySelector('input[name="keyword"]').value
    const params = new URLSearchParams({keyword: keyword,});

    const url = `/inventory/${tab}/content?${params}`;
    const response = await fetch(url);
    const html = await response.text();
    mainContainer.innerHTML = html;

    mainTab.forEach((tab) => {
      tab.classList.remove('active');
    });
    main.classList.add('active');
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
  const cTab = event.target.closest('.category-tab')
  const aTab = event.target.closest('.alert-tab')

  if(cTab) {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const categoryId = cTab.dataset.categoryId;
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
    cTab.classList.add('active');
    }else if(aTab) {
    event.preventDefault()
    const keyword = document.querySelector('input[name="keyword"]').value
    const alertType = aTab.dataset.type;
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
    aTab.classList.add('active');
    }
  })
}
