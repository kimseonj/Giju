"use client";

import { useEffect, useState, useRef } from "react";
import Image from "next/image";
import Link from "next/link";
import { Minus, Plus, ShoppingBag, Trash2 } from "lucide-react";
import {
  getCart,
  CartItem,
  CartResponse,
  updateCartItemQuantity,
  getCartBuyInfo,
  deleteCartItems,
} from "@/lib/cart";
import { useRouter } from "next/navigation";
import { Address } from "@/types/address";
import {
  saveAddress,
  getAddressList,
  updateAddress,
  deleteAddress,
  AddressPayload,
} from "@/lib/address";
import { createOrder, OrderResponse } from "@/lib/order";
import { loadTossPayments } from "@tosspayments/payment-sdk";
import TossPaymentWidget from "@/components/common/TossPaymentWidget";

// AddressSummary 컴포넌트 CartPage 함수 바깥에 선언
function AddressSummary({
  address,
  onChange,
}: {
  address: Address | null;
  onChange: () => void;
}) {
  if (!address)
    return (
      <button
        className="w-full bg-orange-500 text-white py-2 rounded hover:bg-orange-600"
        onClick={onChange}
      >
        배송지 등록
      </button>
    );
  return (
    <div className="bg-gray-50 p-3 rounded flex justify-between items-center">
      <div>
        <div className="font-medium">
          {address.recipientName} ({address.phoneNumber})
        </div>
        <div className="text-sm text-gray-600">
          {address.roadAddress} {address.detailAddress}
        </div>
        <div className="text-xs text-gray-400 mt-1">
          {address.alias} {address.defaultAddress && "(기본 배송지)"}
        </div>
      </div>
      <button
        className="ml-4 px-3 py-1 bg-orange-500 text-white rounded hover:bg-orange-600"
        onClick={onChange}
      >
        배송지 변경
      </button>
    </div>
  );
}

export default function CartPage() {
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const [shippingFee, setShippingFee] = useState(0);
  const [finalTotal, setFinalTotal] = useState(0);
  const [buyInfo, setBuyInfo] = useState<any>(null);
  const router = useRouter();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [address, setAddress] = useState<Address | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState<Address>({
    recipientName: "",
    phoneNumber: "",
    roadAddress: "",
    detailAddress: "",
    alias: "",
    defaultAddress: false,
    postcode: 0,
    buildingName: "",
    extraAddress: "",
    addressId: 0,
  });
  const [isEdit, setIsEdit] = useState(false);

  // 결제위젯 관련 ref 및 상태
  const paymentWidgetRef = useRef<any>(null);
  const agreementWidgetRef = useRef<any>(null);
  const [widgetLoaded, setWidgetLoaded] = useState(false);

  // 주문 생성 및 결제에 필요한 정보 준비
  const [orderInfo, setOrderInfo] = useState<any>(null);
  const [amountForWidget, setAmountForWidget] = useState<number>(0);

  useEffect(() => {
    async function fetchCart() {
      try {
        const cart: CartResponse = await getCart();
        setCartItems(cart.items);
        setTotalPrice(cart.totalPrice);
        setShippingFee(cart.deliveryCharge);
        setFinalTotal(cart.totalPriceWithDelivery);
      } catch (e) {
        alert("장바구니 조회 실패");
      }
    }
    fetchCart();
  }, []);

  useEffect(() => {
    if (cartItems.length === 0) {
      setBuyInfo(null);
      return;
    }
    const fetchBuyInfo = async () => {
      try {
        const info = await getCartBuyInfo(cartItems.map((item) => item.cartId));
        setBuyInfo(info);
      } catch (e) {
        setBuyInfo(null);
      }
    };
    fetchBuyInfo();
  }, [cartItems]);

  useEffect(() => {
    async function fetchAddresses() {
      try {
        const list = (await getAddressList()) as Address[];
        setAddresses(list);
        const defaultAddr =
          list.find((a: Address) => a.defaultAddress) || list[0] || null;
        setAddress(defaultAddr);
      } catch (e) {
        setAddresses([]);
        setAddress(null);
      }
    }
    fetchAddresses();
  }, []);

  // 카카오(다음) 우편번호 스크립트 동적 로드
  useEffect(() => {
    if (typeof window !== "undefined" && !(window as any).daum) {
      const script = document.createElement("script");
      script.src =
        "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
      script.async = true;
      document.body.appendChild(script);
    }
  }, []);

  const increaseQuantity = async (id: number, currentQuantity: number) => {
    const newQuantity = currentQuantity + 1;
    try {
      const result = (await updateCartItemQuantity(id, newQuantity)) as any;
      setCartItems((prev) =>
        prev.map((item) =>
          item.cartId === id ? { ...item, quantity: newQuantity } : item
        )
      );
      setTotalPrice(result.data.cartTotalPrice);
      setFinalTotal(result.data.cartTotalPrice + shippingFee);
    } catch (e) {
      alert("수량 변경 실패");
    }
  };

  const decreaseQuantity = async (id: number, currentQuantity: number) => {
    if (currentQuantity <= 1) return;
    const newQuantity = currentQuantity - 1;
    try {
      const result = (await updateCartItemQuantity(id, newQuantity)) as any;
      setCartItems((prev) =>
        prev.map((item) =>
          item.cartId === id ? { ...item, quantity: newQuantity } : item
        )
      );
      setTotalPrice(result.data.cartTotalPrice);
      setFinalTotal(result.data.cartTotalPrice + shippingFee);
    } catch (e) {
      alert("수량 변경 실패");
    }
  };

  const removeItem = async (id: number) => {
    try {
      await deleteCartItems([id]);
      setCartItems(cartItems.filter((item) => item.cartId !== id));
    } catch (e) {
      alert("삭제 실패");
    }
  };

  const openModal = (editAddress?: Address) => {
    if (editAddress) {
      setForm({
        ...editAddress,
        extraAddress: editAddress.extraAddress || "",
        buildingName: editAddress.buildingName || "",
      });
      setIsEdit(true);
    } else {
      setForm({
        recipientName: "",
        phoneNumber: "",
        roadAddress: "",
        detailAddress: "",
        alias: "",
        defaultAddress: addresses.length === 0,
        postcode: 0,
        buildingName: "",
        extraAddress: "",
        addressId: 0,
      });
      setIsEdit(false);
    }
    setShowModal(true);
  };

  const handleModalSave = async () => {
    try {
      const addressToSave: AddressPayload = {
        recipientName: form.recipientName,
        phoneNumber: form.phoneNumber,
        alias: form.alias,
        defaultAddress: form.defaultAddress,
        postcode: form.postcode,
        roadAddress: form.roadAddress,
        buildingName: form.buildingName || "",
        detailAddress: form.detailAddress,
      };
      if (isEdit && form.addressId) {
        await updateAddress(form.addressId, addressToSave);
      } else {
        await saveAddress(addressToSave);
      }
      const list = (await getAddressList()) as Address[];
      setAddresses(list);
      const defaultAddr =
        list.find((a: Address) => a.defaultAddress) || list[0] || null;
      setAddress(defaultAddr);
      setShowModal(false);
    } catch (e) {
      alert("주소 저장/수정 실패");
    }
  };

  const handleDeleteAddress = async (id: number) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await deleteAddress(id);
      const list = (await getAddressList()) as Address[];
      setAddresses(list);
      const defaultAddr =
        list.find((a: Address) => a.defaultAddress) || list[0] || null;
      setAddress(defaultAddr);
    } catch (e) {
      alert("주소 삭제 실패");
    }
  };

  const handleSelectDefault = async (id: number) => {
    try {
      const addr = addresses.find((a) => a.addressId === id);
      if (!addr) return;
      const addressToUpdate: AddressPayload = {
        recipientName: addr.recipientName,
        phoneNumber: addr.phoneNumber,
        alias: addr.alias,
        defaultAddress: true,
        postcode: addr.postcode,
        roadAddress: addr.roadAddress,
        buildingName: addr.buildingName || "",
        detailAddress: addr.detailAddress,
      };
      await updateAddress(id, addressToUpdate);
      const list = (await getAddressList()) as Address[];
      setAddresses(list);
      const defaultAddr =
        list.find((a: Address) => a.defaultAddress) || list[0] || null;
      setAddress(defaultAddr);
    } catch (e) {
      alert("기본 배송지 변경 실패");
    }
  };

  // AddressForm 컴포넌트 분리
  function AddressForm({
    address,
    onSave,
    onClose,
  }: {
    address?: Address;
    onSave: (form: AddressPayload, id?: number) => void;
    onClose: () => void;
  }) {
    const [form, setForm] = useState<Address>(
      address || {
        recipientName: "",
        phoneNumber: "",
        roadAddress: "",
        detailAddress: "",
        alias: "",
        defaultAddress: false,
        postcode: 0,
        buildingName: "",
        extraAddress: "",
        addressId: 0,
      }
    );
    const isEdit = !!address;

    // 카카오 주소 찾기
    const handleFindPostcode = () => {
      if ((window as any).daum && (window as any).daum.Postcode) {
        new (window as any).daum.Postcode({
          oncomplete: function (data: any) {
            setForm((f) => ({
              ...f,
              roadAddress: data.roadAddress,
              postcode: Number(data.zonecode),
              buildingName: data.buildingName || "",
            }));
          },
        }).open();
      } else {
        alert("주소 검색 기능을 불러오지 못했습니다.");
      }
    };

    return (
      <div className="bg-white rounded-lg p-6 w-full max-w-lg shadow-lg z-10">
        <h4 className="font-semibold mb-2">
          {isEdit ? "배송지 수정" : "배송지 등록"}
        </h4>
        <div className="space-y-2">
          <input
            className="w-full border rounded px-3 py-2"
            placeholder="받는 분의 이름을 입력해주세요"
            value={form.recipientName}
            onChange={(e) =>
              setForm((f) => ({ ...f, recipientName: e.target.value }))
            }
          />
          <input
            className="w-full border rounded px-3 py-2"
            placeholder="휴대폰번호를 입력해주세요"
            value={form.phoneNumber}
            onChange={(e) =>
              setForm((f) => ({ ...f, phoneNumber: e.target.value }))
            }
          />
          <div className="flex gap-2">
            <input
              className="flex-1 border rounded px-3 py-2"
              placeholder="우편번호"
              type="number"
              value={form.postcode}
              onChange={(e) =>
                setForm((f) => ({ ...f, postcode: Number(e.target.value) }))
              }
            />
            <button
              className="border px-3 py-2 rounded bg-gray-100 hover:bg-orange-100 text-sm"
              type="button"
              onClick={handleFindPostcode}
            >
              주소 찾기
            </button>
          </div>
          <input
            className="w-full border rounded px-3 py-2 bg-gray-50"
            placeholder="도로명주소"
            value={form.roadAddress || ""}
            readOnly
          />
          <input
            className="w-full border rounded px-3 py-2 bg-gray-50"
            placeholder="참고항목"
            value={form.extraAddress || ""}
            readOnly
          />
          <input
            className="w-full border rounded px-3 py-2 bg-gray-50"
            placeholder="아파트/건물이름"
            value={form.buildingName || ""}
            readOnly
          />
          <input
            className="w-full border rounded px-3 py-2"
            placeholder="상세주소"
            value={form.detailAddress}
            onChange={(e) =>
              setForm((f) => ({ ...f, detailAddress: e.target.value }))
            }
          />
          <input
            className="w-full border rounded px-3 py-2"
            placeholder="별칭"
            value={form.alias}
            onChange={(e) => setForm((f) => ({ ...f, alias: e.target.value }))}
          />
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={form.defaultAddress}
              onChange={(e) =>
                setForm((f) => ({ ...f, defaultAddress: e.target.checked }))
              }
            />{" "}
            기본 배송지로 설정
          </label>
        </div>
        <div className="flex justify-end gap-2 mt-6">
          <button
            className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300"
            onClick={onClose}
          >
            닫기
          </button>
          <button
            className="px-4 py-2 rounded bg-orange-500 text-white hover:bg-orange-600"
            onClick={() =>
              onSave(
                {
                  recipientName: form.recipientName,
                  phoneNumber: form.phoneNumber,
                  alias: form.alias,
                  defaultAddress: form.defaultAddress,
                  postcode: form.postcode,
                  roadAddress: form.roadAddress,
                  buildingName: form.buildingName || "",
                  detailAddress: form.detailAddress,
                },
                form.addressId
              )
            }
          >
            {isEdit ? "수정" : "저장"}
          </button>
        </div>
      </div>
    );
  }

  // AddressListModal 컴포넌트 내부에서 showForm 상태에 따라 AddressForm을 띄우고, 저장 시 handleModalSave 등 기존 저장 로직을 연결
  function AddressListModal({
    addresses,
    selectedId,
    onSelect,
    onEdit,
    onAdd,
    onDelete,
    onClose,
  }: {
    addresses: Address[];
    selectedId: number | null;
    onSelect: (addr: Address) => void;
    onEdit: (form: AddressPayload, id?: number) => void;
    onAdd: (form: AddressPayload) => void;
    onDelete: (id: number) => void;
    onClose: () => void;
  }) {
    const [showForm, setShowForm] = useState(false);
    const [editAddress, setEditAddress] = useState<Address | undefined>(
      undefined
    );

    const handleAdd = () => {
      setEditAddress(undefined);
      setShowForm(true);
    };
    const handleEdit = (addr: Address) => {
      setEditAddress(addr);
      setShowForm(true);
    };
    const handleFormClose = () => setShowForm(false);
    const handleFormSave = async (form: AddressPayload, id?: number) => {
      if (editAddress) {
        await onEdit(form, id);
      } else {
        await onAdd(form);
      }
      setShowForm(false);
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-lg shadow-lg">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-bold">배송지 목록</h3>
            <button onClick={onClose} aria-label="닫기">
              <span className="text-2xl">×</span>
            </button>
          </div>
          <button
            className="w-full mb-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600"
            onClick={handleAdd}
          >
            배송지 추가하기
          </button>
          <div className="mb-4 max-h-48 overflow-y-auto">
            {addresses.length === 0 ? (
              <div className="text-gray-500 text-sm">
                등록된 배송지가 없습니다.
              </div>
            ) : (
              addresses.map((addr, idx) => (
                <div
                  key={addr.addressId ?? idx}
                  className="flex items-center justify-between border-b py-2"
                >
                  <div className="flex items-center gap-2">
                    <input
                      type="radio"
                      checked={selectedId === addr.addressId}
                      onChange={() => onSelect(addr)}
                    />
                    <div>
                      <div className="font-medium">
                        {addr.recipientName} ({addr.phoneNumber}){" "}
                        {addr.defaultAddress && (
                          <span className="ml-2 text-xs text-orange-500">
                            [기본배송지]
                          </span>
                        )}
                      </div>
                      <div className="text-sm text-gray-600">
                        {addr.roadAddress} {addr.detailAddress}
                      </div>
                      <div className="text-xs text-gray-400 mt-1">
                        {addr.alias}
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      className="px-2 py-1 text-xs bg-gray-200 rounded hover:bg-orange-100"
                      onClick={() => handleEdit(addr)}
                    >
                      수정
                    </button>
                    {!addr.defaultAddress && (
                      <button
                        className="px-2 py-1 text-xs bg-red-100 text-red-600 rounded hover:bg-red-200"
                        onClick={() => onDelete(addr.addressId)}
                      >
                        삭제
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
          {showForm && (
            <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
              <AddressForm
                address={editAddress}
                onSave={handleFormSave}
                onClose={handleFormClose}
              />
            </div>
          )}
          <button
            className="w-full mt-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
            onClick={onClose}
          >
            닫기
          </button>
        </div>
      </div>
    );
  }

  // 배송지 추가
  const handleAddAddress = async (form: AddressPayload) => {
    await saveAddress(form);
    const list = (await getAddressList()) as Address[];
    setAddresses(list);
    setAddress(list.find((a: Address) => a.defaultAddress) || list[0] || null);
  };
  // 배송지 수정
  const handleEditAddress = async (payload: AddressPayload, id?: number) => {
    if (!id) return;
    await updateAddress(id, payload);
    const list = (await getAddressList()) as Address[];
    setAddresses(list);
    const updated = list.find(
      (a: Address) => String(a.addressId) === String(id)
    );
    setAddress(updated || list[0] || null);
    setShowModal(false);
  };

  // 주문 생성 및 결제에 필요한 정보 준비
  const handlePrepareOrder = async () => {
    if (!cartItems.length) {
      alert("장바구니가 비어있습니다.");
      return;
    }
    if (!address) {
      alert("배송지를 선택해주세요.");
      return;
    }
    try {
      const orderResult = await createOrder(
        cartItems.map((item) => item.cartId)
      );
      setOrderInfo({
        orderId: orderResult.orderId,
        orderName: orderResult.orderName,
        customerName: orderResult.customerName,
        customerEmail: orderResult.customerEmail,
      });
      setAmountForWidget(orderResult.amount);
    } catch (e) {
      alert("주문 생성 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="bg-gray-100 min-h-screen">
      <div className="container mx-auto px-4 py-8 md:py-12">
        <div className="mb-6 flex items-center">
          <button
            type="button"
            onClick={() => router.push("/")}
            className="mr-3 focus:outline-none text-main font-jj font-36pt cursor-pointer"
            aria-label="뒤로가기"
          >
            &lt; 장바구니
          </button>
          {/* <span className="jj-title">장바구니</span> */}
        </div>

        {cartItems.length === 0 ? (
          <div className="text-center py-12">
            <h2 className="text-xl font-medium mb-4">
              장바구니가 비어있습니다
            </h2>
            <p className="text-gray-500 mb-6">상품을 추가해 주세요</p>
            <Link
              href="/products"
              className="inline-block bg-primary text-white px-4 py-2 rounded hover:opacity-90 transition"
            >
              쇼핑 계속하기
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
              {cartItems.map((item) => (
                <div
                  key={item.cartId}
                  className="rounded-lg border bg-white p-4 flex items-center"
                >
                  <div className="flex-shrink-0 mr-4">
                    <Image
                      src={item.imageUrl || "/images/placeholder.png"}
                      alt={item.drinkName}
                      width={80}
                      height={80}
                      className="rounded-md object-cover"
                    />
                  </div>
                  <div className="flex-grow">
                    <h3 className="font-medium">{item.drinkName}</h3>
                    <p className="text-gray-500 text-sm mt-1">
                      {item.unitPrice.toLocaleString()}원
                    </p>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      className="h-8 w-8 border rounded flex items-center justify-center hover:bg-gray-100 disabled:opacity-50"
                      onClick={() =>
                        decreaseQuantity(item.cartId, item.quantity)
                      }
                      disabled={item.quantity <= 1}
                    >
                      <Minus className="h-4 w-4" />
                    </button>
                    <span className="w-8 text-center">{item.quantity}</span>
                    <button
                      className="h-8 w-8 border rounded flex items-center justify-center hover:bg-gray-100"
                      onClick={() =>
                        increaseQuantity(item.cartId, item.quantity)
                      }
                    >
                      <Plus className="h-4 w-4" />
                    </button>
                  </div>
                  <div className="ml-4 text-right min-w-[100px]">
                    <div className="font-medium">
                      {(item.unitPrice * item.quantity).toLocaleString()}원
                    </div>
                    <button
                      className="text-red-500 hover:text-red-700 mt-1 text-sm flex items-center"
                      onClick={() => removeItem(item.cartId)}
                    >
                      <Trash2 className="h-4 w-4 mr-1" />
                      삭제
                    </button>
                  </div>
                </div>
              ))}
              <div className="mt-6 flex justify-start">
                <Link
                  href="/"
                  className="w-[100px] h-[42px] flex items-center justify-center text-[#0E2E40] font-medium text-sm"
                  style={{
                    backgroundImage: "url('/white-button.svg')",
                    backgroundSize: "100% 100%",
                    backgroundPosition: "center",
                    backgroundRepeat: "no-repeat",
                    border: "none",
                  }}
                >
                  쇼핑 계속하기
                </Link>
              </div>
            </div>

            <div>
              <div className="rounded-lg border p-6">
                <h2 className="text-xl font-bold mb-4">주문 요약</h2>

                {/* 배송지 섹션 */}
                <div className="mb-4">
                  <AddressSummary
                    address={(() => {
                      console.log("AddressSummary address:", address);
                      return address;
                    })()}
                    onChange={() => setShowModal(true)}
                  />
                </div>
                {showModal && (
                  <AddressListModal
                    addresses={addresses}
                    selectedId={address?.addressId || null}
                    onSelect={(addr) => {
                      setAddress(addr);
                      setShowModal(false);
                    }}
                    onEdit={handleEditAddress}
                    onAdd={handleAddAddress}
                    onDelete={handleDeleteAddress}
                    onClose={() => setShowModal(false)}
                  />
                )}

                <div className="space-y-3 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500">상품 금액</span>
                    <span>
                      {buyInfo ? buyInfo.totalPrice.toLocaleString() : 0}원
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">배송비</span>
                    <span>
                      {buyInfo
                        ? buyInfo.deliveryCharge === 0
                          ? "무료"
                          : `${buyInfo.deliveryCharge.toLocaleString()}원`
                        : "무료"}
                    </span>
                  </div>
                  <hr className="border-gray-200 my-2" />
                  <div className="flex justify-between font-bold">
                    <span>총 결제 금액</span>
                    <span>
                      {buyInfo
                        ? buyInfo.totalPriceWithDelivery.toLocaleString()
                        : 0}
                      원
                    </span>
                  </div>
                </div>
                <div className="flex justify-center">
                  {/* 기존 결제 버튼 대신 아래 위젯 렌더링 */}
                  {orderInfo ? (
                    <TossPaymentWidget
                      amount={amountForWidget}
                      orderInfo={orderInfo}
                    />
                  ) : (
                    <button
                      className="w-[140px] h-[68px] mt-6 text-white rounded flex items-center justify-center hover:opacity-90 transition"
                      style={{
                        backgroundImage: "url('/cart-button.svg')",
                        backgroundSize: "100% 100%",
                        backgroundPosition: "center",
                        backgroundRepeat: "no-repeat",
                        border: "none",
                      }}
                      onClick={handlePrepareOrder}
                    >
                      구매하기
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
