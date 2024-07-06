'use client';

import Link from "next/link";
import {Popover, PopoverButton, PopoverPanel, Transition} from "@headlessui/react";
import {Bars3Icon, XMarkIcon} from "@heroicons/react/24/solid";
import {Fragment} from "react";

export default function Navigation() {
    return(
        <Popover className={"container mx-auto flex items-center border-b-2 px-6 py-2 h-24"}>
            <h1 className="font-bold">Rand and File</h1>
            <div className="grow">
                <div className="hidden sm:flex items-center justify-center gap-2 md:gap-8">
                    <Link href="/">Home</Link>
                    <Link href="congress">Congress</Link>
                </div>
            </div>

            <div className="flex grow items-center justify-end sm:hidden">
                <PopoverButton className="inline-flex items-center justify-center rounded-md bg-white p-2 text-gray-400
            hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500">
                    <span className="sr-only">Open menu</span>
                    <Bars3Icon className="h-6 w-6" aria-hidden="true" />
                </PopoverButton>
            </div>

            <Transition
                as={Fragment}
                enter="duration-200 easeout"
                enterFrom="opacity-0 scale-95"
                enterTo="opactiy-100 scale-100"
                leave="duration100 ease-in"
                leaveFrom="opacity-100 scale-100"
                leaveTo="opacity-0 scale-95"
            >
                <PopoverPanel focus className="absolute inset-x-0 top-0 origin-top-right transform p-2 transition md:hidden">
                    <div className="rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5 divide-y-2 divide-gray-50">
                        <div className="px-5 pt-5 pb-6">
                            <div className="flex items-center justify-between">
                                <h1 className="font-bold"></h1>
                                <div className="-mr-2">
                                    <PopoverButton className="inline-flex items-center justify-center rounded-md bg-white p-2 text-gray-400
            hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500">
                                        <span className="sr-only">Close menu</span>
                                        <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                                    </PopoverButton>
                                </div>
                            </div>
                            <div className="mt-6">
                                <nav className="grid gap-y-8">
                                    <Link className="focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500 px-2"
                                          href="/"
                                    >
                                        Home
                                    </Link>
                                    <Link className="focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500 px-2"
                                          href="congress"
                                    >
                                        Congress
                                    </Link>
                                </nav>
                            </div>
                            <div className="mt-6 flex flex-col items-center gap-2">
                                <Link href="register"
                                      className="rounded-md bg-white px-4 py-2 text-sm font-medium text-black md:text-x1 w-full border-2
                              focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500"
                                >
                                    Register
                                </Link>
                                <Link href="login"
                                      className="rounded-md bg-gray-500 px-4 py-2 text-sm font-medium md:text-x1 w-full
                              focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500"
                                >
                                    Login
                                </Link>
                            </div>
                        </div>
                    </div>
                </PopoverPanel>
            </Transition>

            <div className="hidden sm:block">
                <Link href="register" className="mr-2 font-bold">
                    Register
                </Link>
                <Link href="login" className="font-bold">
                    Log In
                </Link>
            </div>

        </Popover>
    );
}